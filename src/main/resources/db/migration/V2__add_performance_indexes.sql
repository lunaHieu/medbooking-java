CREATE INDEX idx_users_role ON users(Role);
CREATE INDEX idx_appointments_status ON appointments(Status);
CREATE INDEX idx_appointments_starttime ON appointments(StartTime);
CREATE INDEX idx_doctoravailability_starttime ON doctoravailability(StartTime);
