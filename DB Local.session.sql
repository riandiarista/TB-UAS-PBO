CREATE TABLE supply (
    supply_number SERIAL PRIMARY KEY, -- ID unik untuk setiap supply
    type VARCHAR(50) NOT NULL,        -- Jenis BBM
    quantity INT NOT NULL,            -- Jumlah BBM dalam liter
    price NUMERIC(10, 2) NOT NULL,    -- Harga per liter
    added_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Tanggal penambahan
    id_supplier VARCHAR(50) NOT NULL  -- ID supplier terkait
);
SELECT setval(pg_get_serial_sequence('supply', 'supply_number'), COALESCE((SELECT MAX(supply_number) FROM supply), 0));
