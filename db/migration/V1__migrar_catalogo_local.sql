-- PostgreSQL / Supabase. Ejecutar una vez después de respaldar la base de datos.
-- No borra ni actualiza filas existentes. Las categorías se identifican por nombre
-- sin distinguir mayúsculas y los productos por nombre + marca + modelo.
BEGIN;

CREATE TEMP TABLE catalogo_local (
  categoria text, nombre text, marca text, modelo text, precio numeric, stock integer,
  descripcion text, imagen text
) ON COMMIT DROP;

INSERT INTO catalogo_local VALUES
                               ('Notebooks','ASUS ROG Strix G16 (G614JV)','asus','G614JV',1390000,6,'Intel Core i7-13650HX, RTX 4060, 16 GB DDR5, SSD 1 TB.','/img/productos/ASUS ROG Strix G16 (G614JV).png'),
                               ('Tarjetas Gráficas','ASUS Dual GeForce RTX 4070 OC 12GB','asus','RTX 4070 OC 12GB',619990,4,'GPU NVIDIA GeForce RTX 4070 OC, 12 GB GDDR6X.','/img/productos/asus-dual-rtx-4070.png'),
                               ('Monitores','LG UltraGear 27GP850-B 27" QHD 165Hz','lg','27GP850-B',379990,8,'Monitor Nano IPS QHD de 27 pulgadas y 165 Hz.','/img/productos/lg-ultragear-27gp850.jpg'),
                               ('Perifericos','Logitech G Pro X Superlight 2','logitech','G Pro X Superlight 2',109990,15,'Mouse inalámbrico con sensor HERO 2.','/img/productos/logitech-g-pro-x-superlight-2.png'),
                               ('Consolas','PlayStation 5 Slim','sony','PS5 Slim',499990,3,'Consola PS5 Slim con SSD de 1 TB.','/img/productos/playstation-5-slim.png'),
                               ('Almacenamiento','Samsung 990 PRO 1TB NVMe SSD','samsung','990 PRO 1TB',99990,20,'SSD NVMe PCIe 4.0 de 1 TB.','/img/productos/samsung-990-pro-1tb.png'),
                               ('Procesadores','AMD Ryzen 7 7800X3D','amd','Ryzen 7 7800X3D',389990,10,'Procesador de 8 núcleos con AMD 3D V-Cache.','/img/productos/amd-ryzen-7-7800x3d.png'),
                               ('Placas Madre','ASUS ROG Strix B650-A Gaming WiFi','asus','ROG Strix B650-A',249990,7,'Placa madre AM5 B650 con WiFi 6E.','/img/productos/asus-rog-strix-b650-a.png'),
                               ('Memorias RAM','Corsair Vengeance RGB DDR5 32GB (2x16GB) 6000MHz','corsair','Vengeance RGB DDR5 32GB',129990,25,'Memoria DDR5 6000 MHz, kit de 32 GB.','/img/productos/corsair-vengeance-rgb-ddr5-32gb.png'),
                               ('Fuentes de Poder','Corsair RM850x 850W 80+ Gold','corsair','RM850x 850W',139990,12,'Fuente modular 850 W con certificación 80 Plus Gold.','/img/productos/corsair-rm850x.png'),
                               ('Gabinetes','NZXT H510 Flow','nzxt','H510 Flow',74990,14,'Gabinete Mid-Tower ATX con panel frontal perforado.','/img/productos/nzxt-h510-flow-2.png'),
                               ('Refrigeración','Corsair iCUE H100i Elite Capellix 240mm','corsair','H100i Elite Capellix 240mm',139990,9,'Refrigeración líquida AIO de 240 mm.','/img/productos/corsair-h100i-elite-capellix.png'),
                               ('PCs Armados','PC Armado GameHub Ryzen 5 Edition','gamehub','Ryzen 5 Edition',749990,5,'PC armado Ryzen 5, RTX 4060, 16 GB DDR5 y SSD de 1 TB.','/img/productos/pc-armado-ryzen-5-edition.png');

INSERT INTO categorias (nombre, descripcion, estado)
SELECT DISTINCT categoria, 'Migrada desde catálogo local', true
FROM catalogo_local c
WHERE NOT EXISTS (SELECT 1 FROM categorias x WHERE lower(trim(x.nombre)) = lower(trim(c.categoria)));

INSERT INTO productos (nombre, marca, modelo, precio, stock, descripcion, imagen, estado, categoria_id)
SELECT c.nombre, c.marca, c.modelo, c.precio, c.stock, c.descripcion, c.imagen, true, cat.id
FROM catalogo_local c
         JOIN categorias cat ON lower(trim(cat.nombre)) = lower(trim(c.categoria))
WHERE NOT EXISTS (
    SELECT 1 FROM productos p
    WHERE lower(trim(p.nombre)) = lower(trim(c.nombre))
      AND lower(trim(coalesce(p.marca, ''))) = lower(trim(coalesce(c.marca, '')))
      AND lower(trim(coalesce(p.modelo, ''))) = lower(trim(coalesce(c.modelo, '')))
);

COMMIT;
