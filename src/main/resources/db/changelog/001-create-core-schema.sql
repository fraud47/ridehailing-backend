CREATE TABLE riders (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50) NOT NULL UNIQUE,
    email_address VARCHAR(255) NOT NULL UNIQUE,
    rating NUMERIC(3, 2) NOT NULL
);

CREATE TABLE vehicles (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    registration_number VARCHAR(50) NOT NULL UNIQUE,
    make VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    color VARCHAR(100) NOT NULL,
    year_of_manufacture INTEGER NOT NULL,
    seat_capacity INTEGER NOT NULL
);

CREATE TABLE drivers (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50) NOT NULL UNIQUE,
    license_number VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL,
    rating NUMERIC(3, 2) NOT NULL,
    current_latitude NUMERIC(10, 7) NOT NULL,
    current_longitude NUMERIC(10, 7) NOT NULL,
    vehicle_id UUID UNIQUE,
    CONSTRAINT fk_drivers_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id)
);

CREATE TABLE rides (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    rider_id UUID NOT NULL,
    driver_id UUID,
    status VARCHAR(30) NOT NULL,
    pickup_address VARCHAR(255) NOT NULL,
    dropoff_address VARCHAR(255) NOT NULL,
    pickup_latitude NUMERIC(10, 7) NOT NULL,
    pickup_longitude NUMERIC(10, 7) NOT NULL,
    dropoff_latitude NUMERIC(10, 7) NOT NULL,
    dropoff_longitude NUMERIC(10, 7) NOT NULL,
    distance_in_km NUMERIC(10, 2) NOT NULL,
    quoted_fare NUMERIC(10, 2) NOT NULL,
    actual_fare NUMERIC(10, 2),
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    assigned_at TIMESTAMP WITH TIME ZONE,
    picked_up_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_rides_rider FOREIGN KEY (rider_id) REFERENCES riders (id),
    CONSTRAINT fk_rides_driver FOREIGN KEY (driver_id) REFERENCES drivers (id)
);
