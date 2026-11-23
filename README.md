# NexusHire — Job Portal

NexusHire is a full-stack job portal built with **Java 17, Spring Boot and MySQL**. Employers can post jobs and review applicants, job seekers can browse and apply for jobs, and admins moderate everything from a dedicated dashboard. The interface is a modern, responsive SaaS-style UI built with Thymeleaf templates and custom CSS.

> Built as a college project.

---

## Features

### Job Seeker
- Register and log in with a role picker
- Browse and search approved jobs
- View job details and apply with one click (one application per job)
- Track applications and their status on **My applications**
- Manage profile and resume details

### Employer
- Post new jobs (jobs go to the admin for approval first)
- Edit or delete your own job postings
- View applicants for each job
- Update application status: `APPLIED` → `UNDER_REVIEW` → `SHORTLISTED` → `REJECTED` / `HIRED`

### Admin
- Dashboard overview
- Review and approve pending jobs
- View and manage all jobs
- View and manage all users

### General
- Role-based access (Employer, Job Seeker, Admin) using session-based login
- Newly posted jobs default to `PENDING`; only `APPROVED` jobs appear in search
- Responsive sidebar and top-bar layout shared across pages

---

## Tech Stack

| Layer      | Technology                              |
|------------|-----------------------------------------|
| Language   | Java 17                                 |
| Framework  | Spring Boot 3.3.5                       |
| Templating | Thymeleaf                               |
| Database   | MySQL (plain JDBC via MySQL Connector)  |
| Build tool | Maven                                   |
| Frontend   | HTML, CSS, Lucide icons, Inter font     |

---

## Project Structure

```
nexushire-portal/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/nexushire/
    │   ├── NexusHireApplication.java     # Spring Boot entry point
    │   ├── App.java                      # Console demo (optional)
    │   ├── web/                          # Controllers
    │   ├── service/                      # Business logic (e.g. AuthService)
    │   ├── dao/                          # JDBC data access classes
    │   ├── model/                        # Entities and enums
    │   └── util/                         # DBConnection, PasswordUtil
    └── resources/
        ├── application.properties
        ├── static/css/                   # Stylesheets
        └── templates/                    # Thymeleaf pages
            ├── login.html, register.html
            ├── dashboard.html, jobs.html, job-detail.html
            ├── apply.html, my-applications.html, profile.html
            ├── my-jobs.html, job-form.html, job-edit.html
            ├── employer-applicants.html
            ├── admin-jobs.html, admin-all-jobs.html, admin-users.html
            └── shell.html                # Shared sidebar / top bar layout
```

---

## Prerequisites

- **JDK 17** or newer
- **Maven 3.8+**
- **MySQL 8+**
- An IDE such as VS Code (with the Java Extension Pack) or IntelliJ IDEA

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/nexushire-portal.git
cd nexushire-portal
```

### 2. Create the database

Log in to MySQL and create a database:

```sql
CREATE DATABASE nexushire;
```

The app uses three main tables: `users`, `jobs` and `applications`. Create them using the SQL script included in this repository (add the file name here, e.g. `schema.sql`). Note that `applications` has a `UNIQUE(job_id, seeker_id)` constraint so a job seeker can apply to a job only once.

### 3. Configure the database connection

Open `src/main/resources/application.properties` and update the values to match your setup:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/nexushire
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
```

If your database connection is configured in `DBConnection.java` instead, update the URL, username and password there.

> **Security note:** never commit real database passwords to GitHub. Use placeholders in the committed file, or keep your local credentials in an untracked config file.

### 4. Run the application

Using Maven:

```bash
mvn spring-boot:run
```

Or build and run the jar:

```bash
mvn clean package
java -jar target/nexushire-portal-*.jar
```

Or in VS Code, open `NexusHireApplication.java` and click **Run**.

### 5. Open in the browser

```
http://localhost:8080
```

---

## Default Accounts

On first run the console demo and seed data create sample accounts for each role (Admin, Employer, Job Seeker). Add the demo credentials here for anyone testing the project, for example:

| Role       | Email                | Password   |
|------------|----------------------|------------|
| Admin      | `admin@example.com`  | `********` |
| Employer   | `employer@example.com` | `********` |
| Job Seeker | `seeker@example.com` | `********` |

> Change or remove these credentials before deploying anywhere public.

---

## How It Works

1. A user registers or logs in and picks a role.
2. **Employers** post jobs. New jobs are created with status `PENDING`.
3. **Admins** review pending jobs and approve them.
4. Only `APPROVED` jobs are visible to job seekers in search.
5. **Job seekers** apply to a job. Each application starts as `APPLIED`.
6. **Employers** review applicants and move them through `UNDER_REVIEW`, `SHORTLISTED`, `REJECTED` or `HIRED`.
7. Job seekers see the updated status on **My applications**.

---

## Screenshots

Add screenshots of your app here. Save the images in a `docs/screenshots/` folder and reference them like this:

```markdown
![Login](docs/screenshots/login.png)
![Admin Dashboard](docs/screenshots/admin-dashboard.png)
![Browse Jobs](docs/screenshots/browse-jobs.png)
```

---

## Troubleshooting

- **CSS or new templates not showing (404 on `/css/app.css`):** do a clean rebuild with `mvn clean package`, then run again.
- **Cannot connect to MySQL:** check that MySQL is running and that the URL, username and password in your configuration are correct.
- **Port 8080 already in use:** set `server.port=8081` in `application.properties`.

---

## Future Improvements

- Spring Security with password hashing and CSRF protection
- Email notifications when an application status changes
- Resume upload and download
- Pagination and advanced job filters
- Unit and integration tests

---

## Author

**<Your Name>**
GitHub: [@your-username](https://github.com/your-username)

---

## License

This project was created for educational purposes. Add a license of your choice (for example MIT) if you plan to share or reuse it.
