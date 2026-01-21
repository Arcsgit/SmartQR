# SmartQR

**QR Code Management Platform with real-time analytics**

Live Demo: [smartqr-code.site](https://smartqr-code.site)

---
## ABOUT THE PROJECT

SmartQR is a modern web application that allows users to:
- Generate custom QR codes for URLs and text
- Update QR destinations without regenerating codes
- Track scan analytics with real-time data
- Visualize metrics with interactive charts
- Manage all QR codes from a centralized dashboard

Built with Angular, Spring Boot, and deployed on AWS infrastructure.

---  
## FEATURES

### Core Features:
- User Authentication (Signup/Login with JWT)
- Dynamic QR Code Generation
- QR Code Management (Edit, Delete, Download)
- Real-time Scan Analytics
- Geographic Location Tracking
- Device Information Analytics
- Interactive Dashboard with Charts
- Responsive Design (Mobile & Desktop)
### Analytics Features:
- Total scan count
- Scan timeline charts
- Device breakdown (Mobile/Desktop)
- Geographic distribution
- Real-time updates

---
## TECHNOLOGY STACK

### Frontend :
- Angular Framework
- Pure HTML & CSS
- Chart.js
- RxJS
- ngx-toastr
### Backend :
- Spring Boot 3.x
- Java 21
- Spring Security
- JWT Authentication
- Spring Data JPA
### Database :
- PostgreSQL (EC2 Instance)
### Cloud Services:
- AWS EC2 (Backend, Frontend Hosting, PostgreSQL)
- AWS S3 (QR Image Storage)
### Tools & Libraries:
- Maven
- ZXing (QR Code Generation)
- IPInfo API (Geolocation)
- Nginx (Reverse Proxy)
- Let's Encrypt (SSL Certificate)

---
## DATA FLOW

1. User creates QR code via Angular frontend
2. Frontend sends request to Spring Boot API
3. Backend generates QR image using ZXing
4. QR image uploaded to AWS S3
5. Metadata saved in PostgreSQL
6. S3 URL returned to user

---
## INSTALLATION & SETUP
### Backend Setup (Spring Boot)
1. Clone the repository:  
	`git clone https://github.com/Arcsgit/SmartQR`  
	`cd smartqr/backend`
2. Create a .env file in backend root:  
	`touch .env`
3. Build the project:  
	`mvn clean package -DskipTests`
4. Run locally:  
	`java -jar target/smartqr-0.0.1-SNAPSHOT.jar`
5. Backend will start on: http://localhost:8080
### Frontend Setup (Angular)
1. Navigate to frontend directory:  
	`cd smartqr/frontend`
2. Install dependencies:  
	`npm install`
3. Run development server:  
	`ng serve`
4. Open browser: http://localhost:4200

---
## SCREENSHOTS

1. Landing Page  
![Landing Page](landing_page.png)

2. QR Code Generation

3. QR Code Management Dashboard

4. Analytics Dashboard

---
## CONTACT

###### Email: ars.archit6@gmail.com
###### LinkedIn:[ linkedin.com/in/architrshet](https://linkedin.com/in/architrshet)
###### GitHub: [github.com/Arcsgit](https://github.com/Arcsgit)

Built by [Archit Shet]

January 2026
