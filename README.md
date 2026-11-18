# Document Storage Service

A simple document storage application built using Spring Boot and Amazon S3. It allows users to search for their documents by filename and download them through REST API endpoints.

## About the Project

The application uses a single Amazon S3 bucket to store documents for multiple users. Each user has a separate folder (prefix) inside the bucket, so their files can be searched independently.

For example:

```text
rakshith-user-document-storage/
└── sandy/
    ├── logistics_invoice.pdf
    └── logistics_report.pdf
```

## Features

- Search documents using a filename or part of a filename.
- Search is case-insensitive.
- Retrieve documents belonging to a particular user.
- Download files directly from Amazon S3.
- Check file ownership before allowing a download.
- Handle multiple files stored under a user's folder.

## Technologies Used

- Java 21
- Spring Boot
- Maven
- Amazon S3
- AWS SDK for Java 2.x
- JUnit 5
- Mockito

## API Usage

Start the application on port `8080` before making requests.

### Check application status

**GET**
```text
http://localhost:8080/api/users/health
```

### Search documents

**GET**
```text
http://localhost:8080/api/users/sandy/files/search?name=logistics
```

This searches for files containing `logistics` in the `sandy` folder.

Example response:

```json
{
  "userName": "sandy",
  "searchTerm": "logistics",
  "totalFiles": 2,
  "files": [
    {
      "fileName": "logistics_invoice.pdf",
      "key": "sandy/logistics_invoice.pdf"
    },
    {
      "fileName": "logistics_report.pdf",
      "key": "sandy/logistics_report.pdf"
    }
  ]
}
```

### Download a document

**GET**
```text
http://localhost:8080/api/users/sandy/files/download?key=sandy/logistics_invoice.pdf
```

The file is downloaded from S3. The API checks that the requested file belongs to the specified user.

## AWS Configuration

The application connects to Amazon S3 using the AWS SDK default credentials provider.

Add the following properties to `application.properties`:

```properties
spring.application.name=document-storage
server.port=8080

aws.s3.bucket-name=YOUR_BUCKET_NAME
aws.s3.region=YOUR_AWS_REGION
```

Configure your AWS credentials locally using:

```bash
aws configure
```

The IAM user should have the required S3 permissions to list and download objects from the bucket.

**Note:** AWS access keys and secret keys should never be committed to the repository.

## Running the Project

Clone the repository and open the project directory.

Run the application on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

## Running Tests

Run the following command to execute the unit tests:

```powershell
.\mvnw.cmd clean test
```

## Security

The S3 bucket is kept private, and public access is not required. The download service validates the requested S3 key against the user's folder before retrieving a document.

---

**Developed by Rakshith K.**