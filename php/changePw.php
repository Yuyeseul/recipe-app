<?php
// 데이터베이스 연결
$con = mysqli_connect("", "", "", "");
mysqli_query($con, 'SET NAMES utf8');

// POST로부터 데이터 수신
$id = isset($_POST["id"]) ? $_POST["id"] : "";
$newPassword = isset($_POST["pw"]) ? $_POST["pw"] : "";

// 비밀번호 업데이트 쿼리
$statement = mysqli_prepare($con, "UPDATE user SET pw = ? WHERE id = ?");
mysqli_stmt_bind_param($statement, "ss", $newPassword, $id);
$result = mysqli_stmt_execute($statement);

// 응답 배열 초기화
$response = array();

// 쿼리 실행 결과에 따른 응답
if ($result) {
    $response["success"] = true; // 업데이트 성공
} else {
    $response["success"] = false; // 업데이트 실패
}

// JSON 형식으로 응답 출력
echo json_encode($response);

// 데이터베이스 연결 종료
mysqli_stmt_close($statement);
mysqli_close($con);
?>
