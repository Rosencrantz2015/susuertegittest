<?php
require_once("../../db_connection/db_connection.php");

$accion         = $_POST['hdn-frm-action'];
$txtNombres	    = $_POST['txt-nombres'];
$txtApellidos	= $_POST['txt-apellidos'];
$txtIdent       = $_POST['txt-num-identificacion'];
$txtEmail	    = $_POST['txt-email'];
$txtCelular     = $_POST['txt-telefono'];
$hdnClienteId   = $_POST['hdn-cliente-id'];

if($db){
    switch ($accion){
        case "nuevo":
            $qryInsertCliente = "INSERT INTO tercero(
            cedula_nit, nombres, apellidos, telefono, celular, 
            email, tipo_tercero_id)
            VALUES ('$txtIdent', '$txtNombres', '$txtApellidos', '', '$txtCelular', '$txtEmail', 1)";            
            
                $filasAfectadas = $db->exec($qryInsertCliente);
                echo "Cliente Creado Correctamente";
                //echo "<pre>$qryInsertCliente</pre>";
            break;
    
        case "edicion":
            $qryUpdateCliente = "UPDATE tercero
                                SET cedula_nit='$txtIdent', nombres='$txtNombres', apellidos='$txtApellidos',
                                    telefono='', celular='$txtCelular', email='$txtEmail'
                                WHERE tercero_id = $hdnClienteId";
            
            //$filasAfectadas = $db->exec($qryUpdateCliente);
            echo "$qryUpdateCliente";   
            //echo "Cliente Editado Satisfactoriamente";
            break;
    }
} 
?>