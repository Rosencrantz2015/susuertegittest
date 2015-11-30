<?php
    require_once("../db_connection/db_connection.php");
    
    /* Definir links para las paginas */
    $link_principal = "../index.php";
    $link_producto = "producto.php";
    $link_cliente = "cliente.php";

    
?>

<!DOCTYPE html>

<html>
<head>
    <title> Administraci&oacute;n de Clientes </title>
    <meta name="viewport" content="width=device-width, initial-scale=1">	
    <link href="../css/bootstrap.min.css" rel="stylesheet">
    <link href="../css/jquery-ui.min.css" rel="stylesheet">
    <link href="../css/jquery-ui.theme.min.css" rel="stylesheet">
    <link href="../css/jquery-ui.structure.min.css" rel="stylesheet">
    <link href="../css/font-awesome.min.css" rel="stylesheet">
	<link href="../css/cafeteria-global-styles.css" rel="stylesheet">
	<link href="../css/bootstrap-navbar-custom.css" rel="stylesheet">
	
	
    <style>
		
	</style>    
    
</head>

<body>
    <?php require_once("../snipets/html/menu_principal.php")?>
    
    <!-- Contenido de la pagina -->
    <div class="container-fluid">
        <div class="row">
            <div class="col-md-12">
				<button type="button" id="btn-nuevo-cliente" class="btn btn-primary" data-toggle="modal" data-target="#modal-crear-cliente">
					<span class="fa fa-user-plus" aria-hidden="true"></span> Agregar Cliente
				</button>
                <h3> Listado de Clientes </h3>
                <div class="input-group">
                    <input type="text" id="txt-buscar-cliente" class="form-control" placeholder="Buscar cliente por nombres, apellidos o c&eacute;dula" />
                    <span class="input-group-addon">
                        <i class="fa fa-search"></i>
                    </span>                    
                </div>
                <table class="table table-striped table-bordered table-hover table-condensed" style="margin-top:0.5em">
                    <thead class="bg-primary">               
                        <th> Nombres </th>               
                        <th> Apellidos </th>
						<th> C&eacute;dula / NIT </th>
                        <th> E-mail </th>
                        <th> Telefono </th>
                    </thead>
                    <tbody id="tbd-clientes" class="tbd-list">
                                  
                    </tbody>
                </table>
            </div>
        
      
        </div> <!-- fin div row -->
		
		<!-- Modal -->
		<div class="modal fade" id="modal-crear-cliente" role="dialog">
			<div class="modal-dialog modal-lg">
				<div class="modal-content">
					<div class="modal-header" style="padding: 5px; background-color:#fff200; color:#21409a;">
					  <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
					  <h3 id="h4-cliente-header"> <span id="spn-nuevo-cliente"><i class="fa fa-user-plus"></i> Crear Nuevo Cliente </span> </h3>
					</div>
					<div class="modal-body">
						<form id="frm-cliente" name="frm-cliente" method="post" action="../snipets/backend/cliente_backend.php">
							<input type="hidden" id="hdn-frm-action" name="hdn-frm-action" value="nuevo">
							<input type="hidden" id="hdn-cliente-id" name="hdn-cliente-id" value="">
						<div class="row">
							<div class="col-md-6">
							  <div class="form-group">
								<label for="txt-nombres">Nombres <span class="text-danger">*</span></label>
								<input type="text" class="form-control " id="txt-nombres" name="txt-nombres" placeholder="Nombres">                        
							  </div>
							  <div class="form-group">
								<label for="txt-apellidos">Apellidos <span class="text-danger">*</span></label>
								<input type="text" class="form-control " id="txt-apellidos" name="txt-apellidos" placeholder="Apellidos">                        
							  </div>						  
							  <div class="form-group">
								<label for="txt-num-identificacion">N&uacute;mero de Identificaci&oacute;n <span class="text-danger">*</span></label>
								<input type="text" class="form-control " id="txt-num-identificacion" name="txt-num-identificacion" placeholder="Cedula o NIT">
							  </div>								
							</div>
							<div class="col-md-6">
							  <div class="form-group">
								<label for="txt-email">E-Mail</label>
								<div class="input-group margin-bottom-sm">
									<span class="input-group-addon"><i class="fa fa-at"></i></span>
									<input type="text" class="form-control " id="txt-email" name="txt-email" placeholder="Correo Electronico del Cliente">
								</div>
							  </div>
							  <div class="form-group">
								<label for="txt-telefono">Tel&eacute;fono</label>
								<div class="input-group margin-bottom-sm">
									<span class="input-group-addon"><i class="fa fa-phone"></i></span>
									<input type="text" class="form-control " id="txt-telefono" name="txt-telefono" placeholder="Tel&eacute;fono del Cliente">
								</div>
							  </div>	
							  <div class="form-group">
								<label for="txt-telefono">Celular</label>
								<div class="input-group margin-bottom-sm">
									<span class="input-group-addon"><i class="fa fa-mobile"></i></span>
									<input type="text" class="form-control " id="txt-celular" name="txt-celular" placeholder="Celular del Cliente">
								</div>							                        
							  </div>													
							  <button type="submit" class="btn btn-primary" id="sub_guardar" name="sub_guardar" aria-label="Left Align">
								 <span class="fa fa-plus-square" aria-hidden="true"></span> Guardar Nuevo
							  </button>
							</form>						
							</div>						
						</div>
						<div id="div-msg-cliente" class="bg-success" style="padding: 1em; margin-top: 1em;"> </div>
					</div>
				</div>
			</div>
		</div>				
		
		
		<div class="modal fade" id="modal-elim-cliente" role="dialog">
		  <div class="modal-dialog">		  
			<!-- Modal content-->
			<div class="modal-content">
			  <div class="modal-header">
				<button type="button" class="close" data-dismiss="modal">&times;</button>
				<h4 class="modal-title">Eliminar Cliente</h4>
			  </div>
			  <div class="modal-body">
				<p>Realmente desea eliminar el cliente <span id="spn-elim-cliente-confirm" class="text-danger"></span>  </p>
			  </div>
			  <div class="modal-footer">
				<button type="button" class="btn btn-default btn-sm" data-dismiss="modal">Cancelar</button>
				<button type="button" class="btn btn-danger btn-sm" id="btnContinuarEliminacion" >Si / Continuar</button>
			  </div>
			</div>
			
		  </div>
		</div>		
		
    </div>

    <!-- Footer de la pagina -->
    <footer class="footer">
      <div class="container">
        <p class="text-muted"> Susuerte S.A 2015, Todos los derechos reservados </p>
      </div>
    </footer>
    
    <!-- Bootstrap debe ir primero -->    
    <script src="../js/jquery-1.11.3.min.js"></script>
    <script src="../js/bootstrap.min.js"></script>
    <script src="../js/jquery-ui.min.js"></script>
	<script src="../js/jquery.form.min.js"></script>
	<script src="../js/jquery.validate.min.js"></script>
    
    <script>
    $(document).ready(function(){
		listarClientes();
        $('#div-msg-cliente').hide();		
        $('#txt-buscar-cliente').keyup( listarClientes );
		
        // Al hacer clic en algun cliente
        $('#tbd-clientes').on('click','tr', function(){
			var idCliente = $(this).attr('id');
			var nombreCliente = $(this).children().first().text();			
			$('#sub_guardar').removeClass('btn-primary')
				.addClass('btn-warning')
				.html('<span class="fa fa-edit" aria-hidden="true"></span> Editar Usuario');
			$('#h4-cliente-header').html('<span id="spn-nuevo-cliente"><i class="fa fa-edit"></i> Editar Cliente');						
			$('#modal-crear-cliente').modal('show');
			$('#hdn-frm-action').val('edicion');
           traerCliente(idCliente);          
        });
		
        // Al hacer clic en el boton nuevo
        $('#btn-nuevo-cliente').on('click', function(){
			$('#h4-cliente-header').html('<span id="spn-nuevo-cliente"><i class="fa fa-user-plus"></i> Crear Nuevo Cliente');			
			$('#spn-nuevo-cliente').html('<i class="fa fa-user-plus text-primary"></i> Nuevo Cliente ');
			$('#sub_guardar').removeClass( 'btn-warning' )
                            .addClass( 'btn-primary' )
                            .html( '<span class="fa fa-plus-square" aria-hidden="true"></span> Guardar Nuevo' );                            
           $('#frm-cliente')[0].reset();
		   $('#txt-nombre-cliente').focus();
           $('#hdn-frm-action').val('nuevo');
        });
		
		$('#btn-eliminar').on('click', function(){
			var nombreCliente = $('#txt-nombre-cliente').val();
			$('#spn-elim-cliente-confirm').html('<mark>' + nombreCliente + '</mark>');
		});
        
		// Eliminar Producto
		$('#btnContinuarEliminacion').on('click',function(){
			var idCliente = $('#hdn-cliente-id').val();
			$.ajax({
				type: 'POST',
				url: '../snipets/ajax/eliminar_cliente.php',
				data: ({ idCliente : idCliente })        
			}).done(function(datos){
				formPorDefecto();
				$('#txt-buscar-cliente').val('');
				listarClientes();
				$('#div-msg-cliente').html(datos).show();
				$('#modal-elim-cliente').modal('hide');
				window.setTimeout(function(){
					$('#div-msg-cliente').fadeOut('slow');    
				},5000);				
			 });
	    });
		
		$('#frm-cliente').validate({
			rules: {
				'txt-nombres':{
					required:true
				},
				'txt-apellidos':{
					required:true
				},
				'txt-num-identificacion':{
					required:true
				}
			},
			messages: {
				'txt-nombres':{
					required:'El nombre del cliente es requerido'
				},
				'txt-apellidos':{
					required:'El apellido del cliente es requerido'
				},
				'txt-num-identificacion':{
					required:'El numero de identificaci&oacute;n es requerido'
				}
				
			}
		});

		//Formulario producto        
		var options = {
			target: '#div-msg-cliente',
			beforeSubmit: function(){			  
			  return $('#frm-cliente').validate().form();                      
			},  
			success: function(){        		
				$.ajax({
					 url: 'cliente.php',					 
					 success: function(datos){               						
						$('#div-msg-cliente').show();
						formPorDefecto();
						listarClientes();
						window.setTimeout(function(){
							$('#div-msg-cliente').fadeOut('slow');    
						},5000);
					 }
				});
				
			}			
		};
		
		$('#frm-cliente').ajaxForm(options); 
        
    }); // Fin jquery
	
	function formPorDefecto() {
		$('#spn-nuevo-cliente').html('<i class="fa fa-user-plus"></i> Nuevo Cliente ');
		$('#sub_guardar').removeClass( 'btn-warning' )
						 .addClass( 'btn-primary' )
						 .html( '<span class="fa fa-user" aria-hidden="true"></span> Guardar Nuevo' );                            
		$('#frm-cliente')[0].reset();		
		$('#hdn-frm-action').val('nuevo');
		$('#btn-nuevo').fadeOut(100);		
	}
    
    function traerCliente( idCliente ) {
       $.ajax({
           type: 'POST',
           url: '../snipets/ajax/info_cliente.php',
           data: ({ idCliente : idCliente })        
       }).done(function(datos){
          var cliente = JSON.parse(datos);
          for (var i in cliente) {
              $('#txt-nombres').val( cliente[i].nombres );
              $('#txt-apellidos').val( cliente[i].apellidos );
              $('#txt-email').val( cliente[i].email );
              $('#txt-telefono').val( cliente[i].telefono );
			  $('#txt-celular').val( cliente[i].celular );
			  $('#txt-num-identificacion').val( cliente[i].cedula_nit);
			  $('#hdn-cliente-id').val( cliente[i].tercero_id );
          }          
       }); 
    }
	
    var listarClientes = function(){       
            $('#tbd-clientes').html('');
            $.ajax({
                type: 'POST',
                url: '../snipets/ajax/busqueda_generica.php',
                data: ({ cadena : $('#txt-buscar-cliente').val(),
                         tabla : 'cliente' 
                       })
            }).done(function(datos){
               var clientes = JSON.parse(datos);
               //console.log(clientes);
               for (var i in clientes) {
                 $('#tbd-clientes').append( '<tr id="'+clientes[i].tercero_id+'">'
                                             +'<td>'+ clientes[i].nombres +'</td>'
                                             +'<td>'+ clientes[i].apellidos +'</td>'
											 +'<td>'+ clientes[i].cedula_nit +'</td>'
                                             +'<td>'+ clientes[i].email +'</td>'
                                             +'<td>'+ clientes[i].celular +'</td>'                                             
                                             + '</tr>'); 
               }
            });
        
    }
    </script>
</body>
</html>
