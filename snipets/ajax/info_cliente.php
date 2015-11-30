<?php

    require_once("../../db_connection/db_connection.php");

        $idCliente = $_POST['idCliente'];
       
        $arr_cliente = array();
        
        $qry_cliente = "SELECT tercero_id, cedula_nit, nombres, apellidos, telefono, celular, 
                        email, tipo_tercero_id
                        FROM tercero WHERE tercero_id = $idCliente";
                           
        $rs = $db->prepare($qry_cliente);
        $rs->execute();
        
        while( $fila=$rs->fetch(PDO::FETCH_ASSOC) ){
            $arr_cliente[] = $fila;      
        }
        
        echo json_encode($arr_cliente);        
?>