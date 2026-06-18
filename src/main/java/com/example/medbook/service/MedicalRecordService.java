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

    @Autowired
    private FileStorageService fileStorageService;

    public MedicalRecordService(){
        // Cloudinary uploads don't need local directory initialization
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
        String secureUrl = fileStorageService.uploadFile(file);
        ExamResults examResults = new ExamResults();
        examResults.setMedicalRecord(record);
        examResults.setFilePath(secureUrl);
        examResults.setFileType(file.getContentType());
        examResults.setFileDescription(fileDescription);

        ExamResults savedResult = examResultsRepository.save(examResults);
        return medicalRecordMapper.toExamResultResponse(savedResult);
    }

    public List<MedicalRecordResponse> getDoctorMedicalRecords(Integer doctorId) {
        List<MedicalRecord> records = medicalRecordRepository.findByDoctor_DoctorId(doctorId);
        return convertToResponseList(records);
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

    public List<MedicalRecordResponse> convertToResponseList(List<MedicalRecord> records) {
        if (records == null) return null;
        return records.stream().map(this::convertToResponse).collect(java.util.stream.Collectors.toList());
    }

    public List<MedicalRecordResponse> getAllMedicalRecords(Integer patientId) {
        List<MedicalRecord> records;
        if (patientId != null) {
            User patient = userRepository.findById(patientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Bệnh nhân không tồn tại."));
            records = medicalRecordRepository.findByAppointment_Patient(patient);
        } else {
            records = medicalRecordRepository.findAll();
        }
        return convertToResponseList(records);
    }

    public MedicalRecordResponse getMedicalRecordById(Integer id) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ bệnh án ID: " + id));
        return convertToResponse(record);
    }

    @Transactional
    public void deleteMedicalRecord(Integer id) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ bệnh án ID: " + id));
        
        // cascade delete of associated exam results is configured on the entity as CascadeType.ALL,
        // so deleting the record will automatically delete its exam results.
        medicalRecordRepository.delete(record);
    }
}