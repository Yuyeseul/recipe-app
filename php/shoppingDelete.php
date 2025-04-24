<?php
// 데이터베이스 연결 설정
$con = mysqli_connect("", "", "", "");
if (!$con) {
    die("Connection failed: " . mysqli_connect_error());
}

mysqli_query($con, 'SET NAMES utf8');

// URL 경로에서 nickname과 shoppingName 가져오기
if (isset($_SERVER['PATH_INFO'])) {
    $path = explode('/', $_SERVER['PATH_INFO']);
    if (count($path) == 3) { // 3개 인자가 있는지 확인
        $nickname = $path[1];
        $shoppingName = $path[2];

        // SQL 쿼리 준비 및 실행
        $statement = mysqli_prepare($con, "DELETE FROM shopping WHERE nickname = ? AND shoppingName = ?");
        mysqli_stmt_bind_param($statement, "ss", $nickname, $shoppingName);
        mysqli_stmt_execute($statement);

        $response = array();
        $response["success"] = mysqli_stmt_affected_rows($statement) > 0;
        $response["message"] = $response["success"] ? "shopping deleted successfully" : "No shopping found";

        mysqli_stmt_close($statement);
    } else {
        $response = array("success" => false, "message" => "Invalid input");
    }
} else {
    $response = array("success" => false, "message" => "Invalid input");
}

// JSON 응답 반환
header('Content-Type: application/json'); // JSON 응답 헤더 설정
echo json_encode($response);

// 연결 종료
mysqli_close($con);
?>
