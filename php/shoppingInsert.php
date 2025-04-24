<?php
$con = mysqli_connect("", "", "", "");
mysqli_query($con, 'SET NAMES utf8');

$nickname = isset($_POST["nickname"])?$_POST["nickname"]:"";
$shoppingName = isset($_POST["shoppingName"])?$_POST["shoppingName"]:"";

$statement = mysqli_prepare($con, "INSERT INTO shopping VALUES (?,?)");
mysqli_stmt_bind_param($statement, "ss", $nickname, $shoppingName);
mysqli_stmt_execute($statement);

$response = array();
$response["success"] = true;


echo json_encode($response);

?>