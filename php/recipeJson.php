<?php
$con = mysqli_connect("", "", "", "");
mysqli_query($con, 'SET NAMES utf8');

$query = "SELECT * FROM recipe";
$result = mysqli_query($con, $query);

if (!$result) {
    die('Query failed: ' . mysqli_error($con));
}
$response = array();
$i = 0;

while ($row = mysqli_fetch_array($result)) {
    $member = array();
    
    $member["no"] = $row["no"];
    $member["recipeName"] = $row["recipeName"];
    $member["recipeImage"] = $row["recipeImage"];
    $member["recipeCategory"] = $row["recipeCategory"];
    $member["recipeIngredients"] = $row["recipeIngredients"];
    $member["recipeContent"] = $row["recipeContent"];
    $member["dayTime"] = $row["dayTime"];
    $member["nickname"] = $row["nickname"];
    $member["favorite"] = $row["favorite"];
    $member["count"] = $row["count"];

    $response[$i] = $member;
    $i = $i + 1;
}

$json["recipe"] = $response;
$output = json_encode($json);
echo $output;

mysqli_close($con);
?>
