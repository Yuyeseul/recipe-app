<?php
// 데이터베이스 연결 설정
$con = mysqli_connect("", "", "", "");
if (!$con) {
    die("Connection failed: " . mysqli_connect_error());
}

mysqli_query($con, 'SET NAMES utf8');

// POST 요청으로 nickname과 recipeName 가져오기
if (isset($_POST['nickname']) && isset($_POST['recipeName'])) {
    $nickname = $_POST['nickname'];
    $recipeName = $_POST['recipeName'];

    // SQL 쿼리 준비 및 실행
    $statement = mysqli_prepare($con, "DELETE FROM favorite WHERE nickname = ? AND recipeName = ?");
    mysqli_stmt_bind_param($statement, "ss", $nickname, $recipeName);
    mysqli_stmt_execute($statement);

    $response = array();
    $response["success"] = mysqli_stmt_affected_rows($statement) > 0;
    $response["message"] = $response["success"] ? "favorite deleted successfully" : "No favorite found";

    mysqli_stmt_close($statement);
} else {
    $response = array("success" => false, "message" => "Invalid input");
}

// JSON 응답 반환
header('Content-Type: application/json'); // JSON 응답 헤더 설정
echo json_encode($response);

// 연결 종료
mysqli_close($con);
?>
