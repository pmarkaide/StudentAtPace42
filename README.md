# 42 Student Progress Tracker

This application fetches student data from the 42 API and generates a CSV file containing student progress information across various ranks in the common core curriculum.

## Purpose

The main purpose of this application is to:
- Fetch student data from specified cohorts (Hiver5, Hiver6, Hiver7)
- Retrieve quest completion information
- Calculate the progress of each student through the common core ranks
- Generate a comprehensive CSV report showing days buffer for each rank

## Requirements

- Docker and Docker Compose
- 42 API credentials (UID and Secret)

## Setup and Running

1. Create a `.env` file in the project root with your 42 API credentials:
   ```
   UID_42=your_42_api_uid
   SECRET_42=your_42_api_secret
   ```
   *Important: Do not include quotes around the values*

2. Run the application using Docker:
   ```bash
   # create a output folder
   mkdir -p output
   
   # First time or after code changes
   docker-compose up --build
   
   # Run the container to update the csv file 
   docker-compose up

   ```

3. The CSV file will be generated in the `output` directory of your project root.

## Output

The generated CSV file (`student_progress.csv`) will be located in the `output` directory of your project.

## Appendix: CSV Contents

The CSV file contains the following columns:

| Column | Description |
|--------|-------------|
| Cohort | Student's cohort (e.g., Hiver5, Hiver6, Hiver7) |
| Login | Student's login identifier |
| First Name | Student's first name |
| Last Name | Student's last name |
| Active Rank Buffer | Days buffer for the student's current active rank |
| Pool Month | Month when the student completed the pool |
| Pool Year | Year when the student completed the pool |
| Profile URL | URL to the student's 42 profile |
| Graph URL | URL to the student's project graph |
| Common Core Rank 00 | Days buffer for Rank 00 completion |
| Common Core Rank 01 | Days buffer for Rank 01 completion |
| Common Core Rank 02 | Days buffer for Rank 02 completion |
| Common Core Rank 03 | Days buffer for Rank 03 completion |
| Common Core Rank 04 | Days buffer for Rank 04 completion |
| Common Core Rank 05 | Days buffer for Rank 05 completion |
| Common Core Rank 06 | Days buffer for Rank 06 completion |

### Understanding Days Buffer

The "Days Buffer" represents the difference between the deadline and the actual completion date:
- Positive values: Student completed the rank ahead of schedule (X days early)
- Negative values: Student completed the rank behind schedule (X days late)
- Empty: Rank not yet completed

Each cohort has specific start dates and deadlines for rank completion, which are used to calculate the buffer values.