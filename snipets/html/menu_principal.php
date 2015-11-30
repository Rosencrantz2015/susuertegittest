<!-- Barra de navegacion de la pagina -->
<nav class="navbar navbar-custom navbar-static-top">
  <div class="container">
    <div class="navbar-header">
      <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
        <span class="sr-only">Toggle navigation</span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
      <a class="navbar-brand" href="<?php echo $link_principal ?>"> Susuerte S.A <i class="fa fa-star-half-o"></i> </a>
    </div>
    <div id="navbar" class="collapse navbar-collapse">
      <ul class="nav navbar-nav">
        <li><a href="<?php echo $link_principal ?>">Compra</a></li>
        <li><a href="<?php echo $link_cliente ?>">Cliente</a></li>            
        <li><a href="<?php echo $link_producto ?>">Producto</a></li>
        <li class="dropdown">
          <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">Otros
            <span class="caret"></span>
          </a>
          <ul class="dropdown-menu" role="menu">
            <li><a href="#">Registrar Pago</a></li>
            <li><a href="#">Crear Nuevo Usuario del Sistema</a></li>
            <li class="divider"></li>
            <li class="dropdown-header">Reportes</li>
            <li><a href="#">Saldos Cliente</a></li>                
          </ul>
        </li>
      </ul>
    </div><!--/.nav-collapse -->
  </div>
</nav>