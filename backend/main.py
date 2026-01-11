from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from routers import users, cars, services, bookings, teams

app = FastAPI(title="Car Wash API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(users.router)
app.include_router(cars.router)
app.include_router(services.router)
app.include_router(bookings.router)
app.include_router(teams.router)


@app.get("/")
def root():
    return {"message": "API is running"}


def main():
    pass


if __name__ == "__main__":
    main()
