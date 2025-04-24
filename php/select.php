<?php
// MySQL 데이터베이스에 연결
$con = mysqli_connect("", "", "", "");

// 문자 집합을 UTF-8로 설정
mysqli_query($con, 'SET NAMES utf8');

// // 데이터베이스 연결 오류 확인
// if (!$con)  
// {  
//     echo "MySQL 접속 에러 : ";
//     echo mysqli_connect_error(); // 연결 오류 메시지 출력
//     exit(); // 오류 발생 시 스크립트 종료
// }  

// // // 문자 집합을 UTF-8로 설정
// // mysqli_set_charset($con, "utf8"); 

// // SQL 쿼리 작성: 'recipe' 테이블의 모든 데이터 선택
// $sql = "SELECT * FROM recipe";

// // SQL 쿼리 실행
// $result = mysqli_query($con, $sql);

// // 데이터를 저장할 배열 초기화
// $data = array();   

// // 쿼리 결과가 성공적으로 반환되었는지 확인
// if($result) {  
    
//     // 결과 집합에서 각 행을 읽어 배열에 추가
//     while($row = mysqli_fetch_array($result)) {
//         $member = array();
//         $member["recipeImage"] = $row["recipeImage"];
//         $member["recipeName"] = $row["recipeName"];

//         $data[$i] = $member;
        
//         $i = $i + 1;
//     }

//     // // 배열을 읽기 쉽게 출력
//     // echo "<pre>"; 
//     // print_r($data); 
//     // echo '</pre>';

// }  
// else {  
//     // SQL 쿼리 처리 중 오류 발생 시 오류 메시지 출력
//     echo "SQL문 처리중 에러 발생 : "; 
//     echo mysqli_error($con);
// } 


// $json["recipe"] = $data;
// $output = json_encode($json);
// echo $output;


// // 데이터베이스 연결 종료
// mysqli_close($con);  
// 

if (!$con)  
{  
    echo "MySQL 접속 에러 : ";
    echo mysqli_connect_error();
    exit();  
}  

mysqli_set_charset($con,"utf8"); 


$sql="select * from recipe";

$result=mysqli_query($con,$sql);
$data = array();   
if($result){  
    
    while($row=mysqli_fetch_array($result)){
        array_push($data, 
            array(
            'recipeName'=>$row[1]
        ));
    }

    // echo "<pre>"; print_r($data); echo '</pre>';

    header('Content-Type: application/json; charset=utf8');
    $json = json_encode(array("recipe"=>$data), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
    echo $json;

}  
else{  
    echo "SQL문 처리중 에러 발생 : "; 
    echo mysqli_error($con);
} 

mysqli_close($con);  
?>