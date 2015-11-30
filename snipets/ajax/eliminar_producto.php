<?php
    require_once("../../db_connection/db_connection.php");
    
    $idProducto = $_POST['idProducto'];
    
    $qryEliminarProducto = "DELETE FROM producto WHERE producto_id = $idProducto";
    
    $filasAfectadas = $db->exec($qryEliminarProducto);
    echo "Producto Eliminado Correctamente";
?>