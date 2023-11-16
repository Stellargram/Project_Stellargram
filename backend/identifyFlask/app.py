from flask import Flask, jsonify, request
from flask_cors import CORS
from werkzeug.utils import secure_filename
from include.model.identifyLow import identify_with_tetra
from service.makeTetraDB import make_database

from service.saveCSVService import  create_star_table, input_csv_data
from service.preprocessImageService import preprocess_image

import os

# 플라스크 객체 인스턴스 생성
app = Flask(__name__)

# CORS 설정. 웹 애플리케이션에서 Cross-Origin 요청에서 자격 증명을 허용하도록 설정하는 부분.
CORS(app, resources={r"/*": {"origins": "*"}})
app.config["CORS_SUPPORTS_CREDENTIALS"] = True

# URL 접두사
PREFIX = "/identify"

def initialize():
    # 최초 1회에만 실행될 csv -> DB 함수
    create_star_table()
    input_csv_data()

current_directory = os.path.dirname(__file__)
file_path = os.path.join(current_directory, 'service', 'your_module.py')

# 최초 1회에만 csv -> DB 함수 실행
initialize()


@app.route('/')
def hello_world():  # put application's code here
    return 'Hello World!'


# @app.route(PREFIX + '/star', methods=['GET'])
# def identify_stars():  # put application's code here
#     result = identify_test()
#     return result

# # 사진 업로드 테스트
# @app.route(PREFIX + '/upload', methods=['GET', 'POST'])
# def upload_file():  # put application's code here
#     if request.method == 'POST':
#         file = request.files['file']
#         formdata = request.form
#         file.save("./static/" + secure_filename(file.filename))
#     return "done"


# 테트라 테스트 처리후 사진 띄움
@app.route(PREFIX + '/tetraIdentify', methods=['GET', 'POST'])
def tetra_identify():
    global result
    # POST로 들어왔다면
    if request.method == 'POST':

        # 요청에서 파일 추출
        file = request.files['file']

        # 파일 전처리
        pre_image=preprocess_image(file)

        # 파일 인식 후 결과 반환
        result = identify_with_tetra(pre_image)
        
    return result

# 테트라 db 커스텀 생성용. 현재 사용하지 않음
@app.route(PREFIX + '/makedb', methods=['GET'])
def tetra_make_database():  # put application's code here
    make_database()
    return "made database"


if __name__ == '__main__':
    app.run()


