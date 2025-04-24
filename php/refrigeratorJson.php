<?php
$con = mysqli_connect("", "", "", "");
mysqli_query($con, 'SET NAMES utf8');

$query = "SELECT * FROM refrigerator";
$result = mysqli_query($con, $query);

if (!$result) {
    die('Query failed: ' . mysqli_error($con));
}
$response = array();
$i = 0;

while ($row = mysqli_fetch_array($result)) {
    $member = array();
    
    $member["nickname"] = $row["nickname"];
    $member["ingredientName"] = $row["ingredientName"];
    
    $response[$i] = $member;
    $i = $i + 1;
}

$json["refrigerator"] = $response;
$output = json_encode($json);
echo $output;

mysqli_close($con);
?>
