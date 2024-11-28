# CO3103-Programming Intergration Project-Backend_Internal

## Yêu cầu:

- [Java](https://www.oracle.com/java/technologies/downloads/) (phiên bản JDK 17)
- IDE để chạy ứng dụng (ưu tiên Intellj)

## Database

Database của ứng dụng được lưu trên Aiven.io và chỉ có thành viên nhóm mới có thể truy cập được. Tuy nhiên database được tạo từ [đây](https://github.com/nguyentantai21042004/CO3103-Programming-Intergration-Project-Backend_Internal/blob/main/database.sql) nên bạn có thể tự tạo cho riêng mình và đổi datasource trong application.yml thành của bạn.

## Cài đặt tính năng gửi email

- Nếu muốn sử dụng tính năng gửi email, tại `src/main/resources/` tạo file sensitive-config.yml chứa các thông tin sau:

```properties
spring:
  mail:
    username: # Email của bạn dùng để gửi mail
    password: # App password email của bạn
```

( Yêu cầu email phải được xác thực 2 bước trước tiên và có dùng tính năng app password. Xem chi tiết [tại đây](https://www.youtube.com/watch?v=MkLX85XU5rU)

## Cài đặt

Clone project, tại `src/main/java/L03/CNPM/Music/MusicBackendApplication.java` start ứng dụng và chạy trên `localhost:8088`

## API Document

Nhóm sử dụng OpenAPI để viết Document, các bạn có thể dùng plugin hỗ trợ OpenAPI từ các IDE để xem hoặc copy toàn bộ code từ file [document.yaml](https://github.com/nguyentantai21042004/CO3103-Programming-Intergration-Project-Backend_Internal/blob/main/document.yaml) và dán vào [đây](https://editor.swagger.io/)
