<?php

    require_once("../../db_connection/db_connection.php");

        $idProducto = $_POST['idProducto'];
       
        $arr_producto = array();
        
        $qry_producto = "SELECT producto_id, nombre, nombre_corto, 
                           tags, valor_unitario FROM producto WHERE producto_id = $idProducto";
                           
        $rs = $db->prepare($qry_producto);
        $rs->execute();
        
        while( $fila=$rs->fetch(PDO::FETCH_ASSOC) ){
            $arr_producto[] = $fila;      
        }
        
        echo json_encode($arr_producto);        
?>