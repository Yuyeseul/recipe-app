<?php
$con = mysqli_connect("", "", "", "");
mysqli_query($con, 'SET NAMES utf8');

$recipeName = isset($_POST["recipeName"])?$_POST["recipeName"]:"";
$recipeImage = isset($_POST["recipeImage"])?$_POST["recipeImage"]:"";
$recipeCategory = isset($_POST["recipeCategory"])?$_POST["recipeCategory"]:"";
$recipeIngredients = isset($_POST["recipeIngredients"])?$_POST["recipeIngredients"]:"";
$recipeContent = isset($_POST["recipeContent"])?$_POST["recipeContent"]:"";

$statement = mysqli_prepare($con, "INSERT INTO recipe VALUES (?,?,?,?,?)");
mysqli_stmt_bind_param($statement, "sssss", $recipeName, $recipeImage, $recipeCategory, $recipeIngredients, $recipeContent);
mysqli_stmt_execute($statement);

$response = array();
$response["success"] = true;


echo json_encode($response);

?>