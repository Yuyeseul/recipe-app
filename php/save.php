<?php
$con = mysqli_connect("", "", "", "");
mysqli_query($con, 'SET NAMES utf8');

$nickname = isset($_POST["nickname"])?$_POST["nickname"]:"";
$ingredientName = isset($_POST["ingredientName"])?$_POST["ingredientName"]:"";

$statement = mysqli_prepare($con, "INSERT INTO refrigerator VALUES (?,?)");
mysqli_stmt_bind_param($statement, "ss", $nickname, $ingredientName);
mysqli_stmt_execute($statement);

$response = array();
$response["success"] = true;


echo json_encode($response);

?>