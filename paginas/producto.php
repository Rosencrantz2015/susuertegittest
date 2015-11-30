<?php
    require_once("../db_connection/db_connection.php");
    
    /* Definir links para las paginas */
    $link_principal = "../index.php";
    $link_producto = "producto.php";
	$link_cliente = "cliente.php";
    
    $trs = "";
?>

<!DOCTYPE html>

<html>
<head>
    <title>Productos Cafeteria</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">	
    <link href="../css/bootstrap.min.css" rel="stylesheet">
    <link href="../css/jquery-ui.min.css" rel="stylesheet">
    <link href="../css/jquery-ui.theme.min.css" rel="stylesheet">
    <link href="../css/jquery-ui.structure.min.css" rel="stylesheet">
    <link href="../css/font-awesome.min.css" rel="stylesheet">
	<link href="../css/cafeteria-global-styles.css" rel="stylesheet">
	<link href="../css/bootstrap-navbar-custom.css" rel="stylesheet">
	
	
    <style>
		thead th{
			background-color: #337ab7;
			color:#fff;
		}
		
		
	</style>    
    
</head>

<body>
    <?php require_once("../snipets/html/menu_principal.php")?>
    
    <!-- Contenido de la pagina -->
    <div class="container-fluid">
        <?php
        
        ?>
        <div class="row">
            <div class="col-md-8">
                <h3> Listado de Productos Cafeteria </h3>
                <div class="input-group">
                    <input type="text" id="txt_buscar_producto" class="form-control" placeholder="Buscar Producto..." />
                    <span class="input-group-addon">
                        <i class="fa fa-search"></i>
                    </span>                    
                </div>
                <table class="table table-striped table-bordered table-hover table-condensed" style="margin-top:0.5em">
                    <thead class="bg-primary">               
                        <th> Nombre </th>               
                        <th> Tags </th>
                        <th> Valor Unitario </th>
                    </thead>
                    <tbody id="tbd_productos" class="tbd-list">
                                 
                    </tbody>
                </table>
            </div>
        
            <div class="col-md-4" id="form_content" style="border-left: 1px solid #ddd;">
                <h3> <span id="spn_nuevo_producto"><i class="fa fa-plus-square text-primary"></i> Nuevo Producto </span> </h3>
                    <form id="frmProducto" name="frmProducto" method="post" action="../snipets/backend/producto_backend.php">
                      <input type="hidden" id="hdnFrmAction" name="hdnFrmAction" value="nuevo">
					   <input type="hidden" id="hdnProductoId" name="hdnProductoId" value="">
                      <div class="form-group">
                        <label for="txtNombreProducto">Nombre del Producto <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" id="txtNombreProducto" name="txtNombreProducto" placeholder="Nombre Completo del Producto">                        
                      </div>
                      <div class="form-group">
                        <label for="txtAliasProducto">Nombre Corto del Producto (Alias)</label>
                        <input type="text" class="form-control" id="txtAliasProducto" name="txtAliasProducto" placeholder="Alias del producto">
                      </div>
                      <div class="form-group">
                        <label for="txtEtiquetasProducto">Etiquetas del Producto</label>
                        <div class="input-group margin-bottom-sm">
                            <span class="input-group-addon"><i class="fa fa-tags"></i></span>
                            <input type="text" class="form-control" id="txtEtiquetasProducto" name="txtEtiquetasProducto" placeholder="Palabras que describen el producto">
                        </div>
                      </div>
                      <div class="form-group">
                        <label for="txtValorUnitario">Valor Unitario del Producto <span class="text-danger">*</span></label>
                        <div class="input-group margin-bottom-sm">
                            <span class="input-group-addon"><i class="fa fa-usd"></i></span>
                            <input type="text" class="form-control text-right" id="txtValorUnitario" name="txtValorUnitario" placeholder="Valor unitario del producto">
                        </div>
                      </div>                      
                      <div class="checkbox">
                        <label>
                          <input type="checkbox"> Activo
                        </label>
						<small style="display: block; margin-top:1em;">Los campos con <span class="text-danger">*</span> son obligatorios.</small>
                      </div>                                            
                      <button type="submit" class="btn btn-primary" id="sub_guardar" name="sub_guardar" aria-label="Left Align">
                         <span class="fa fa-plus-square" aria-hidden="true"></span> Guardar Nuevo
                      </button>
                      <button type="button" class="btn btn-primary" id="btnNuevo" aria-label="Left Align" title=" Nuevo Producto ">
                         <span class="fa fa-plus-square" aria-hidden="true"></span>
                      </button>
					  <button type="button" class="btn btn-danger" data-toggle="modal" data-target="#modalDeleteProduct" id="btnEliminar" aria-label="Left Align" title=" Eliminar Producto ">
                         <span class="fa fa-minus-circle" aria-hidden="true"></span>
                      </button>
					  <div id="divMsgProducto" class="bg-success" style="padding: 1em; margin-top: 1em;">						
					  </div>
                    </form>
					
                    
            </div>            
        </div> <!-- fin div row -->
		
		<!-- Modal -->		
		<div class="modal fade" id="modalDeleteProduct" role="dialog">
		  <div class="modal-dialog">
		  
			<!-- Modal content-->
			<div class="modal-content">
			  <div class="modal-header">
				<button type="button" class="close" data-dismiss="modal">&times;</button>
				<h4 class="modal-title">Modal Header</h4>
			  </div>
			  <div class="modal-body">
				<p>Realmente desea eliminar el producto <span id="spnProductElimConfirm" class="text-danger"></span>  </p>
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
        <p class="text-muted"> Cafeteria UCM 2015, Todos los derechos reservados </p>
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
		listarProductos();
        $('#btnNuevo, #btnEliminar, #divMsgProducto').hide();		
        $('#txt_buscar_producto').keyup( listarProductos );
        
        // Al hacer clic en algun producto
        $('#tbd_productos').on('click','tr', function(){
           var idProducto = $(this).attr('id');
           var nombreProducto = $(this).children().first().text();           
           $('#spn_nuevo_producto').html('<i class="fa fa-pencil-square-o text-warning"></i> Editar Producto <small class="text-warning">'+ nombreProducto + '</small>');
           $('#sub_guardar').removeClass( 'btn-primary' );
           $('#sub_guardar').addClass( 'btn-warning' );
           $('#hdnFrmAction').val('edicion');
		   $('#txtNombreProducto').focus();
           $('#sub_guardar').html( '<span class="fa fa-pencil-square" aria-hidden="true"></span> Actualizar' );
           $('#btnNuevo, #btnEliminar').fadeIn(500);
           
           traerProducto(idProducto);          
        });
        
        // Al hacer clic en el boton nuevo
        $('#btnNuevo').on('click', function(){
           $('#spn_nuevo_producto').html('<i class="fa fa-plus-square text-primary"></i> Nuevo Producto ');
           $('#sub_guardar').removeClass( 'btn-warning' )
                            .addClass( 'btn-primary' )
                            .html( '<span class="fa fa-plus-square" aria-hidden="true"></span> Guardar Nuevo' );                            
           $('#frmProducto')[0].reset();
		   $('#txtNombreProducto').focus();
           $('#hdnFrmAction').val('nuevo');		   
           $('#btnEliminar').fadeOut(500);
		   $(this).fadeOut(500); 
        });
		
		$('#btnEliminar').on('click', function(){
			var nombreProducto = $('#txtNombreProducto').val();
			$('#spnProductElimConfirm').html('<mark>' + nombreProducto + '</mark>');
		});
        
        $('#dlg_producto').dialog({
           width: 450, 
           autoOpen: false,
           position: { my: "center", at: "center", of: window },           
           modal:true
        });
		
		// Eliminar Producto
		$('#btnContinuarEliminacion').on('click',function(){
			var idProducto = $('#hdnProductoId').val();
			$.ajax({
				type: 'POST',
				url: '../snipets/ajax/eliminar_producto.php',
				data: ({ idProducto : idProducto })        
			}).done(function(datos){
				formPorDefecto();
				$('#txt_buscar_producto').val('');
				listarProductos();
				$('#divMsgProducto').html(datos).show();
				$('#modalDeleteProduct').modal('hide');
				window.setTimeout(function(){
					$('#divMsgProducto').fadeOut('slow');    
				},5000);				
			 }); 		
			
	    });
		
		
		$('#frmProducto').validate({
			rules: {
				txtNombreProducto:{
					required:true
				},
				txtValorUnitario:{
					required:true	
				}
			},
			messages: {
				txtNombreProducto:{
					required:'El nombre del producto es requerido'
				},
				txtValorUnitario:{
					required:'Se requiere el valor unitario del producto'	
				}
				
			}
		});

		//Formulario producto        
		var options = {
			target: '#divMsgProducto',
			beforeSubmit: function(){			  
			  return $('#frmProducto').validate().form();                      
			},  
			success: function(){        		
				$.ajax({
					 url: 'producto.php',					 
					 success: function(datos){               						
						$('#divMsgProducto').show();
						formPorDefecto();
						listarProductos();
						window.setTimeout(function(){
							$('#divMsgProducto').fadeOut('slow');    
						},5000);
					 }
				});
				
			}			
		};
		
		$('#frmProducto').ajaxForm(options); 
        
    }); // Fin jquery
	
	function formPorDefecto() {
		$('#spn_nuevo_producto').html('<i class="fa fa-plus-square"></i> Nuevo Producto ');
		$('#sub_guardar').removeClass( 'btn-warning' )
						 .addClass( 'btn-primary' )
						 .html( '<span class="fa fa-plus-square" aria-hidden="true"></span> Guardar Nuevo' );                            
		$('#frmProducto')[0].reset();		
		$('#hdnFrmAction').val('nuevo');
		$('#btnNuevo').fadeOut(100);
		$('#btnEliminar').fadeOut(100); 
	}
    
    function traerProducto( idProducto ) {
       $.ajax({
           type: 'POST',
           url: '../snipets/ajax/info_producto.php',
           data: ({ idProducto : idProducto })        
       }).done(function(datos){
          var producto = JSON.parse(datos);
          for (var i in producto) {
              $('#txtNombreProducto').val( producto[i].nombre );
              $('#txtAliasProducto').val( producto[i].nombre_corto );
              $('#txtEtiquetasProducto').val( producto[i].tags );
              $('#txtValorUnitario').val( producto[i].valor_unitario );
			  $('#hdnProductoId').val( producto[i].producto_id );
          }          
       }); 
    }
	
    var listarProductos = function(){       
            $('#tbd_productos').html('');
            $.ajax({
                type: 'POST',
                url: '../snipets/ajax/busqueda_generica.php',
                data: ({ cadena : $('#txt_buscar_producto').val(),
					     tabla : 'producto' })
            }).done(function(datos){
               var productos = JSON.parse(datos);
               for (var i in productos) {
                 $('#tbd_productos').append( '<tr id="'+productos[i].producto_id+'">'
                                             +'<td>'+ productos[i].nombre +'</td>'
                                             +'<td>'+ productos[i].tags +'</td>'
                                             +'<td class="text-right" ><strong>'+ productos[i].valor_unitario +'</strong></td>'
                                             + '</tr>'); 
               }
            });
        
    }
    </script>
</body>
</html>
