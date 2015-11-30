<?php
    require_once("../../db_connection/db_connection.php");

        $cadenaBusqueda = utf8_decode($_POST['cadena']);        
        $tabla = $_POST['tabla'];
        $qrystring = "";
        $arr_tabla = array();
        
        switch($tabla){
            case "cliente":
                $qrystring = "SELECT tercero_id, cedula_nit, nombres, apellidos, telefono, celular, 
                              email, tipo_tercero_id
                              FROM tercero
                              WHERE nombres
                              ILIKE '%$cadenaBusqueda%'
                              OR apellidos ILIKE '%$cadenaBusqueda%'
                              OR cedula_nit ILIKE '%$cadenaBusqueda%'";
                break;
            case "producto":
                $qrystring = " SELECT producto_id, activo, creado, creadopor, nombre, nombre_corto, tags, valor_unitario
                               FROM producto WHERE nombre ILIKE '%$cadenaBusqueda%' ORDER BY nombre  ";
                break;
        }
       
        $rs = $db->prepare($qrystring);
        $rs->execute();
        
        while( $fila=$rs->fetch(PDO::FETCH_ASSOC) ){
            $arr_tabla[] = $fila;      
        }
       
        echo json_encode($arr_tabla);
    
?>