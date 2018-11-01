<?php
    require_once("db_connection/db_connection.php");
    
    /* Definir links para las paginas */
    $link_principal = "index.php";
    $link_producto = "paginas/producto.php";
	$link_cliente = "paginas/cliente.php";  
	
	/*Programador sis.1 2*/
	/*Agregando comentaro para subir a GitHUb*/

	// this is a php command
?>

<!DOCTYPE html>

<html>
<head>
    <title>Susuerte</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">		
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/jquery-ui.theme.min.css" rel="stylesheet">
    <link href="css/jquery-ui.structure.min.css" rel="stylesheet">
    <link href="css/font-awesome.min.css" rel="stylesheet">
	<link href="css/cafeteria-global-styles.css" rel="stylesheet">
	<link href="css/bootstrap-navbar-custom.css" rel="stylesheet">
    
    <style>
		
	</style>    
    
</head>

<body>
    <?php require_once("snipets/html/menu_principal.php")?>
    
    <!-- Contenido de la pagina -->
    <div class="container-fluid">
        <div class="row">
            <div class="col-md-3">
				
	
            </div>
			
            <div class="col-md-6">
				<h1>Realizar Compra</h1>
				<div class="form-group">
					<label for="txt-cliente">Cliente / Tercero</label>
					<div class="input-group margin-bottom-sm">
						<span class="input-group-addon"><i class="fa fa-user"></i></span>
						<input type="text" class="form-control" id="txt-cliente" name="txt-cliente" placeholder="Cliente del pedido...">
					</div>
				 </div>
				<div id="log"></div>
            </div>
			
            <div class="col-md-3">
			
            </div>            
        </div>
    </div>

    <!-- Footer de la pagina -->
    <footer class="footer">
      <div class="container">
        <p class="text-muted">Place sticky footer content here.</p>
      </div>
    </footer>

    <script src="js/jquery-2.1.3.min.js"></script>
	<script src="js/bootstrap.min.js"></script>
    <script src="js/jquery-ui.min.js"></script>
    
    
    <script>
    $(document).ready(function(){
		function log( message ) {
		  $( "<div>" ).text( message ).prependTo( "#log" );
		  $( "#log" ).scrollTop( 0 );
		}		
		
		$('#txt-cliente').autocomplete({
		source: function( request, response ) {
			  $.ajax({
				url: 'snipets/ajax/cliente_autocomplete.php',
				type: 'POST',
				dataType: 'json',
				data: {
				  q: request.term
				},
				success: function( data ) {
				  response( data );
				}
			  });
			},
			minLength: 3,
			select: function( event, ui ) {
			  log( ui.item ?
				"Selected: " + ui.item.id + 'Value: ' +  ui.item.value
				+ "Correo: " + ui.item.email
				: "Nothing selected, input was " + this.value);
			}
		  });// Fin autocomplete
    });//Fin jquery
    </script>
</body>
</html>
