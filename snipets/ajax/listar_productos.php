<?php
    require_once("../../db_connection/db_connection.php");
    
    $arrProductos = array();
    
    $qry_productos = " SELECT producto_id, activo, creado, creadopor, nombre, nombre_corto, tags, valor_unitario
                       FROM producto ORDER BY nombre ";
                       
    $rs = $db->prepare($qry_productos);
    $rs->execute();												  

    while( $fila=$rs->fetch(PDO::FETCH_ASSOC) ){
        $arrProductos[] = $fila;
    }    
    
     echo json_encode($arrProductos);
?>