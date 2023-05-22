CREATE DATABASE cyborgs;
USE cyborgs;

CREATE TABLE sucursales(
	codigo INT PRIMARY KEY AUTO_INCREMENT,
	nombre VARCHAR(45),
	direccion VARCHAR(45),
	correo VARCHAR(45) NOT NULL,
	telefono VARCHAR(45)
);

CREATE TABLE productos(
	codigo INT PRIMARY KEY AUTO_INCREMENT,
	nombre VARCHAR(45),
	descripcion VARCHAR(45),
	cantidad INT,
	precio FLOAT
);

CREATE TABLE clientes(
	codigo INT PRIMARY KEY AUTO_INCREMENT,
	nombre VARCHAR(45),
	nit VARCHAR(45),
	correo VARCHAR(45),
	genero VARCHAR(5)
);

CREATE TABLE vendedores(
	codigo INT PRIMARY KEY AUTO_INCREMENT,
	nombre VARCHAR(45),
	caja INT,
	ventas INT,
	genero VARCHAR(5),
	password VARCHAR(45)
);

CREATE TABLE facturas (
	no_factura INT PRIMARY KEY,
    codigo_cliente INT,
	codigo_vendedor INT,
	fecha DATE,
	total FLOAT
);

CREATE TABLE detalle_facturas (
	id INT PRIMARY KEY AUTO_INCREMENT,
	no_factura INT,
	codigo_producto INT,
	cantidad INT,
    precio FLOAT,
	subtotal FLOAT
);

ALTER TABLE facturas ADD FOREIGN KEY (codigo_cliente) REFERENCES clientes (codigo);
ALTER TABLE facturas ADD FOREIGN KEY (codigo_vendedor) REFERENCES vendedores (codigo);
ALTER TABLE detalle_facturas ADD FOREIGN KEY (no_factura) REFERENCES facturas (no_factura);
ALTER TABLE detalle_facturas ADD FOREIGN KEY (codigo_producto) REFERENCES productos (codigo);

-- VISTA PARA INTERFAZ DE VENTAS INFORMACION DE FACTURAS
CREATE VIEW informacion_facturas AS
SELECT V.caja AS 'caja',
	F.no_factura AS 'no_factura',
	C.nit AS 'nit',
	C.nombre AS 'nombre',
    F.fecha AS 'fecha',
    F.total AS 'total'
FROM facturas F
INNER JOIN vendedores V on F.codigo_vendedor = V.codigo
INNER JOIN clientes C ON F.codigo_cliente = C.codigo;

-- VISTA PARA FACTURA PRODUCTOS DEL DETALLE FACTURA
DROP VIEW informacion_detalle_facturas;

CREATE VIEW informacion_detalle_facturas AS
SELECT D.no_factura AS 'no_factura',
	D.codigo_producto AS 'codigo',
	P.nombre AS 'nombre',
	D.cantidad AS 'cantidad',
	D.precio AS 'precio',
	D.subtotal AS 'subtotal'
FROM detalle_facturas D
INNER JOIN productos P ON D.codigo_producto = P.codigo;

-- FUNCION PARA COMPROBAR LA CANTIDAD DE PRODUCTOS
DELIMITER //
CREATE FUNCTION fn_comprobar_cantidad(codigo_producto INT, cantidad_venta INT)
RETURNS BOOLEAN
DETERMINISTIC
BEGIN
    DECLARE cantidad_actual INT;
    SET cantidad_actual = (SELECT cantidad FROM productos WHERE codigo = codigo_producto); 
	IF (cantidad_actual >= cantidad_venta AND cantidad_venta != 0) THEN
		RETURN TRUE;
	ELSE
		RETURN FALSE;
	END IF;
END //
DELIMITER ;

-- PROCEDIMIENTO PARA AGREGAR NUEVA FACTURA Y SUMAR VENTA AL VENDEDOR
DELIMITER //
CREATE PROCEDURE sp_generar_factura(no_factura INT, codigo_cliente INT, codigo_vendedor INT, total FLOAT)
BEGIN
	INSERT INTO facturas VALUES (no_factura, codigo_cliente, codigo_vendedor, NOW(), total);
    UPDATE vendedores SET ventas = ventas + 1 WHERE codigo=codigo_vendedor;
END //
DELIMITER ;

-- PROCEDIMIENTO PARA AGREGAR PRODUCTOS AL DETALLE FACTURA Y PARA RESTAR CANTIDAD EN STOCK
DELIMITER //
CREATE PROCEDURE sp_agregar_producto_detalle_factura(no_factura INT, codigo_producto INT, cantidad_venta INT, precio FLOAT)
BEGIN
	DECLARE cantidad_actual INT;
    SET cantidad_actual = (SELECT cantidad FROM productos WHERE codigo = codigo_producto); 
    IF (cantidad_actual >= cantidad_venta) THEN
		UPDATE productos SET cantidad = cantidad_actual - cantidad_venta WHERE codigo=codigo_producto;
		INSERT INTO detalle_facturas VALUES (0, no_factura, codigo_producto, cantidad_venta, precio, cantidad_venta * precio);
    ELSE 
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Error: la cantidad excede a la cantidad en stock';
    END IF;
END //
DELIMITER ;




