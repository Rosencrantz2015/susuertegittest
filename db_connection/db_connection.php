<?php

/*
$dbHost = "localhost";
$dbPuerto = "5432";
$dbUser = "robin";
$dbPass = "b3telgeuse";
$dbname = "cafeteria";
*/

$dbHost = "localhost";
$dbPuerto = "5432";
$dbUser = "postgres";
$dbPass = "";
$dbname = "susuertedb";


try{
	$db = new PDO("pgsql:dbname=$dbname; host=$dbHost",$dbUser,$dbPass);
	$db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_WARNING);
}
catch(PDOException $e){
	echo "Error Chacho".$e->getMessage();
}



?>