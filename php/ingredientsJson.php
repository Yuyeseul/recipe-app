<?php
$con = mysqli_connect("", "", "", "");
mysqli_query($con, 'SET NAMES utf8');

$query = "SELECT * FROM ingredients";
$result = mysqli_query($con, $query);

if (!$result) {
    die('Query failed: ' . mysqli_error($con));
}
$response = array();
$i = 0;

while ($row = mysqli_fetch_array($result)) {
    $member = array();
    
    $member["no"] = $row["no"];
    $member["ingredientName"] = $row["ingredientName"];

    $response[$i] = $member;
    $i = $i + 1;
}

$json["ingredients"] = $response;
$output = json_encode($json);
echo $output;

mysqli_close($con);
?>
