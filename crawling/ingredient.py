import requests
from bs4 import BeautifulSoup
import pandas as pd

# 기본 URL 설정
base_url = "https://sauce.foodpolis.kr/home/specialty/foodDbSearch.do?PAGE_MN_ID=SIS-030101&PAGE_NO="
ingredients = []

# 총 페이지 수 설정
total_pages = 1  

for page in range(1, total_pages + 1):
    try:
        # 각 페이지 URL 생성
        url = f"{base_url}{page}"
        response = requests.get(url)
        response.raise_for_status()  # 요청이 성공했는지 확인

        # BeautifulSoup으로 페이지 파싱
        soup = BeautifulSoup(response.text, 'html.parser')

        # 식재료명 크롤링
        for item in soup.select('table tbody tr td:nth-child(2)'):  # 두 번째 <td> 선택
            name = item.get_text(strip=True)
            ingredients.append(name)

        print(f"페이지 {page}에서 {len(ingredients)}개의 식재료 크롤링 완료.")

    except requests.exceptions.RequestException as e:
        print(f"페이지 {page} 요청 중 오류 발생: {e}")
    except Exception as e:
        print(f"페이지 {page}에서 오류 발생: {e}")

if ingredients:
    # 데이터프레임으로 변환 후 엑셀 파일로 저장
    df = pd.DataFrame(ingredients, columns=["Ingredient Name"])
    df.to_excel("ingredients_all_pages.xlsx", index=False)  # 엑셀 파일로 저장
    print("모든 페이지의 식재료를 엑셀 파일로 저장 완료!")
else:
    print("크롤링된 식재료가 없습니다.")
