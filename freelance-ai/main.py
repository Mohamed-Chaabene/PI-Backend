from typing import List

from fastapi import FastAPI
from fastapi.responses import StreamingResponse
from pydantic import BaseModel

from matching import rank_freelancers
from pricing import predict_rate
from proposal import generate_proposal, stream_proposal


app = FastAPI(title="Freelance AI API")


class MatchJobPayload(BaseModel):
    skills: str
    description: str


class MatchFreelancerPayload(BaseModel):
    name: str
    skills: str
    experience: str
    reviews: str


class MatchRequest(BaseModel):
    job: MatchJobPayload
    freelancers: List[MatchFreelancerPayload]


class ProposalRequest(BaseModel):
    job_description: str
    skills: str
    experience_years: int
    timeline_days: int


class PriceRequest(BaseModel):
    skill: str
    experience_years: int
    rating: float
    location: str


@app.post("/api/match")
def match_route(payload: MatchRequest):
    return rank_freelancers(payload.job.dict(), [f.dict() for f in payload.freelancers])


@app.post("/api/proposal")
def proposal_route(payload: ProposalRequest):
    proposal = generate_proposal(
        payload.job_description,
        payload.skills,
        payload.experience_years,
        payload.timeline_days,
    )
    return {"proposal": proposal}


@app.post("/api/proposal/stream")
def proposal_stream_route(payload: ProposalRequest):
    token_stream = stream_proposal(
        payload.job_description,
        payload.skills,
        payload.experience_years,
        payload.timeline_days,
    )
    return StreamingResponse(token_stream, media_type="text/plain")


@app.post("/api/price")
def price_route(payload: PriceRequest):
    return predict_rate(
        payload.skill,
        payload.experience_years,
        payload.rating,
        payload.location,
    )
