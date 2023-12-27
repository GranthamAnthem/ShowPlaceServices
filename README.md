# Showplace Services

Welcome to Showplace Services, the backend for the Showplace KMP app, providing a seamless experience for discovering the latest shows, bands, and venues in Baltimore.

## Features

- **Shows/Events:** Retrieve a list of shows, bands, and venues.
- **Single Entities:** Obtain detailed information about a specific show, band, or venue.
- **Data Sources:**
  - Utilizes Jsoup for web scraping to gather show data from [Baltshowplace](https://baltshowplace.tumblr.com/).
  - Stores data in a PostgreSQL database using Exposed for database interaction.
  - Hosts the PostgreSQL database on Supabase.
  - Deploys the Showplace backend on AWS Elastic Beanstalk.

## Technologies Used

- **[Ktor](https://ktor.io/):** A powerful Kotlin web framework used for building the backend server.
- **[Serialization](https://github.com/Kotlin/kotlinx.serialization):** Leveraging Kotlin Serialization for efficient data serialization.
- **[PostgreSQL with Exposed](https://github.com/JetBrains/Exposed):** Storing and interacting with data using PostgreSQL as the database and Exposed as the ORM.
- **[Jsoup](https://jsoup.org/):** Enabling web scraping to gather show data.
- **[Supabase](https://supabase.io/):** Hosting the PostgreSQL database with Supabase for easy database management.
- **[AWS Elastic Beanstalk](https://aws.amazon.com/elasticbeanstalk/):** Deploying and hosting the Showplace backend on AWS Elastic Beanstalk.


## Getting Started

To run Showplace Services locally, follow these steps:

1. Clone the project.
2. Open the project in your preferred IDE.
3. Configure the necessary environment variables.
4. Run the application locally.


## API Endpoints

- **GET /shows:** Retrieve a list of all shows.
- **GET /bands:** Get information about all bands.
- **GET /venues:** Get details about all venues.
- **GET /shows/{showId}:** Get information about a specific show.
- **GET /bands/{bandId}:** Get details about a specific band.
- **GET /venues/{venueId}:** Get information about a specific venue.


## License

This project is licensed under the MIT License, making it open for contributions and use.


## Contributing

If you'd like to contribute to Showplace Services, please send me a message.

Enjoy exploring the vibrant world of Baltimore's entertainment with Showplace Services!
