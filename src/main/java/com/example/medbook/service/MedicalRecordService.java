package com.example.medbook.service;

import com.example.medbook.dto.request.MedicalRecordRequest;
import com.example.medbook.dto.response.ExamResultResponse;
import com.example.medbook.dto.response.MedicalRecordResponse;
import com.example.medbook.entity.Appointment;
import com.example.medbook.entity.ExamResults;
import com.example.medbook.entity.MedicalRecord;
import com.example.medbook.entity.User;
import com.example.medbook.exception.ResourceNotFoundException;
import com.example.medbook.mapper.MedicalRecordMapper;
import com.example.medbook.repository.AppointmentRepository;
import com.example.medbook.repository.ExamResultsRepository;
import com.example.medbook.repository.MedicalRecordRepository;
import com.example.medbook.repository.UserRepository;
import com.example.medbook.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class MedicalRecordService {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MedicalRecordMapper medicalRecordMapper;
    @Autowired
    private ExamResultsRepository examResultsRepository;

    private final Path fileStorageLocation = Paths.get("src/main/resources/uploads").toAbsolutePath().normalize();
    public MedicalRecordService(){
        try{
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo thư mực lưu trữ file.",e);
        }
    }
    @Transactional
    public MedicalRecordResponse createMedicalRecord(MedicalRecordRequest request, UserDetailsImpl currentUser) {

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Lịch hẹn ID: " + request.getAppointmentId()));

        if (appointment.getDoctor().getUser().getUserId() != currentUser.getId()) {
            throw new AccessDeniedException("Bạn không phải Bác sĩ chủ trì cuộc hẹn này.");
        }

        if (!"Completed".equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalStateException("Chỉ có thể tạo bệnh án cho lịch hẹn đã hoàn tất (Completed).");
        }

        if (medicalRecordRepository.findByAppointment_AppointmentId(appointment.getAppointmentId()).isPresent()) {
            throw new IllegalStateException("Lịch hẹn này đã có hồ sơ bệnh án rồi.");
        }

        MedicalRecord record = new MedicalRecord();
        record.setPatient(appointment.getPatient());
        record.setDoctor(appointment.getDoctor());
        record.setAppointment(appointment);
        record.setDiagnosis(request.getDiagnosis());
        record.setNotes(request.getNotes());

        MedicalRecord savedRecord = medicalRecordRepository.save(record);

        return convertToResponse(savedRecord);
    }

    public List<MedicalRecordResponse> getMyMedicalRecords(UserDetailsImpl currentUser) {

        User patient = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bệnh nhân không tồn tại."));

        List<MedicalRecord> records = medicalRecordRepository.findByAppointment_Patient(patient);

        return medicalRecordMapper.toMedicalRecordResponseList(records);
    }

    private MedicalRecordResponse convertToResponse(MedicalRecord record) {
        MedicalRecordResponse response = medicalRecordMapper.toMedicalRecordResponse(record);

        response.setPatientName(record.getAppointment().getPatient().getLastName() + " " + record.getAppointment().getPatient().getFirstName());
        response.setDoctorName(record.getAppointment().getDoctor().getUser().getLastName() + " " + record.getAppointment().getDoctor().getUser().getFirstName());

        if (record.getExamResults() != null) {
            response.setExamResults(medicalRecordMapper.toExamResultResponseList(record.getExamResults()));
        }

        return response;
    }

    public ExamResultResponse uploadExamResults(
            Integer recordId,
            String fileDescription,
            MultipartFile file,
            UserDetailsImpl currentUser){
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ bệnh án ID: " + recordId));
        //check quyền sở hữu. Đảm bảo Bác sĩ đang login là chủ của bệnh án này
        if(!record.getAppointment().getDoctor().getUser().getUserId().equals(currentUser.getId())){
            throw new AccessDeniedException("Bạn không có quyền tải file lên hồ sơ này.");
        }
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        try{
            Files.copy(file.getInputStream(),targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex){
            throw new RuntimeException("Không thể lưu trữ file " + fileName + ". Thử lại sau!", ex);
        }
        ExamResults examResults = new ExamResults();
        examResults.setMedicalRecord(record);
        examResults.setFilePath(targetLocation.toString());//Luu duong dan tuyet doi
        examResults.setFileType(file.getContentType());
        examResults.setFileDescription(fileDescription);

        ExamResults savedResult = examResultsRepository.save(examResults);
        return MedicalRecordMapper.INSTANCE.toExamResultResponse(savedResult);
    }
    //Lấy lịch sử khám cho Bác sĩ
    public List<MedicalRecordResponse> getRecordsByPatientId(Integer patientId, UserDetailsImpl currentUser) {
        // Kiểm tra bệnh nhân tồn tại
        if (!userRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Bệnh nhân không tồn tại");
        }

        Integer currentDoctorId = currentUser.getId();

        // Sử dụng hàm tìm kiếm kết hợp cả PatientID VÀ DoctorID
        List<MedicalRecord> records = medicalRecordRepository
                .findByPatient_UserIdAndDoctor_DoctorId(patientId, currentDoctorId);

        return medicalRecordMapper.toMedicalRecordResponseList(records);
    }
    // Hàm cho Staff xem lịch sử (Không check UserID của bác sĩ)
    public List<MedicalRecordResponse> getHistoryForStaff(Integer patientId) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Bệnh nhân không tồn tại"));
        List<MedicalRecord> records = medicalRecordRepository.findByAppointment_Patient(patient);
        return medicalRecordMapper.toMedicalRecordResponseList(records);
    }
}