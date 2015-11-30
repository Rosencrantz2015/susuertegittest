<?php
    require_once("../../db_connection/db_connection.php");

        $cadenaBusqueda = utf8_decode($_POST['q']);        
 
        $qrystring = "SELECT cliente_id AS id, nombre_completo AS value, email AS email
                              FROM cliente
                              WHERE nombre_completo ILIKE '%$cadenaBusqueda%' ORDER BY primer_nombre";
        $rs = $db->prepare($qrystring);
        $rs->execute();
        
        while( $fila=$rs->fetch(PDO::FETCH_ASSOC) ){
            $arr_tabla[] = $fila;      
        }
       
        echo json_encode($arr_tabla);
    
?>