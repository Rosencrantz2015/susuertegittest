<?php
    require_once("../../db_connection/db_connection.php");

        $cadenaBusqueda = utf8_decode($_POST['cadena']);
       
        $arr_productos = array();
        
        $qry_productos = " SELECT producto_id, activo, creado, creadopor, nombre, nombre_corto, tags, valor_unitario
                           FROM producto WHERE nombre ILIKE '%$cadenaBusqueda%' ORDER BY nombre  ";
                        
        $rs = $db->prepare($qry_productos);
        $rs->execute();
        
        while( $fila=$rs->fetch(PDO::FETCH_ASSOC) ){
            $arr_productos[] = $fila;      
        }
        
        echo json_encode($arr_productos);
    
?>