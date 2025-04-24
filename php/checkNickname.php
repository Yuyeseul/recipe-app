<?php
// 데이터베이스 연결 정보
$host = "";
$username = "";
$password = "";
$dbname = "";

// MySQLi를 사용하여 데이터베이스 연결
$con = mysqli_connect($host, $username, $password, $dbname);

// 연결 확인
if (!$con) {
    echo json_encode(["error" => "데이터베이스 연결 실패: " . mysqli_connect_error()]);
    exit;
}

// UTF-8 문자셋 설정
mysqli_set_charset($con, "utf8");

// POST 요청으로 닉네임 가져오기
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $nickname = isset($_POST['nickname']) ? trim($_POST['nickname']) : '';

    // 입력값 검증
    if (!empty($nickname)) {
        // SQL 쿼리 작성 및 실행
        $stmt = $con->prepare("SELECT COUNT(*) FROM user WHERE nickname = ?");
        if ($stmt) {
            $stmt->bind_param("s", $nickname);
            $stmt->execute();
            $stmt->bind_result($count);
            $stmt->fetch();

            // 결과에 따라 응답 생성
            echo json_encode(["available" => $count == 0]);

            $stmt->close();
        } else {
            // 쿼리 준비 실패
            echo json_encode(["error" => "쿼리 준비 실패: " . $con->error]);
        }
    } else {
        // 닉네임이 비어있는 경우 오류 메시지
        echo json_encode(["error" => "닉네임이 필요합니다."]);
    }
} else {
    // 잘못된 요청 방법
    echo json_encode(["error" => "잘못된 요청입니다."]);
}

// 데이터베이스 연결 종료
mysqli_close($con);
?>
