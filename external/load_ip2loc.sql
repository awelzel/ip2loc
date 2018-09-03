CREATE TABLE ipv4 (
	ip_from LONG,
	ip_to LONG,
	country_code VARCHAR,
	country_name VARCHAR,
	region_name VARCHAR,
	city_name VARCHAR,
	latitude DOUBLE,
	longitude DOUBLE,
	zip_code VARCHAR
);
.mode csv
.import external/IP2LOCATION-LITE-DB9.CSV ipv4

CREATE INDEX idx_ipv4_ip_from ON ipv4(ip_from);
