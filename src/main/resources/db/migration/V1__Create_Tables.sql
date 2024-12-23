CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR UNIQUE NOT NULL,
    name VARCHAR(100),
    surname VARCHAR(100)
);

CREATE TABLE room (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE booking (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_room FOREIGN KEY (room_id) REFERENCES room (id),
    CONSTRAINT fk_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
);