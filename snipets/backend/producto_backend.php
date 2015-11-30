<?php
require_once("../../db_connection/db_connection.php");

$accion     = $_POST['hdnFrmAction'];
$txtNombre	= $_POST['txtNombreProducto'];
$txtAlias	= $_POST['txtAliasProducto'];
$txtTags	= $_POST['txtEtiquetasProducto'];
$txtValor   = $_POST['txtValorUnitario'];
$hdnProductoId = $_POST['hdnProductoId'];

$txtValor = !empty($txtValor) ? $txtValor : 0;
    


if($db){
    switch ($accion){
        case "nuevo":
            $qryInsertProduct = "INSERT INTO producto(
                activo, creado, creadopor, nombre, nombre_corto, tags, valor_unitario)
                VALUES (TRUE, now(), 1, '$txtNombre', '$txtAlias', '$txtTags', $txtValor )";            
            
                $filasAfectadas = $db->exec($qryInsertProduct);
                echo "Producto Creado Correctamente";
            break;
    
        case "edicion":
            $qryUpdateProduct = "UPDATE producto
               SET nombre='$txtNombre', nombre_corto='$txtAlias', 
                   tags='$txtTags', valor_unitario=$txtValor
               WHERE producto_id = $hdnProductoId";
            
            $filasAfectadas = $db->exec($qryUpdateProduct);
               
            echo "Producto Editado Satisfactoriamente";
            break;
    }
} 
?>