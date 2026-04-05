import { NextResponse } from 'next/server';

const BACKEND_URL = 'https://transitshark.mzhang.dev';

export async function POST() {
  const res = await fetch(`${BACKEND_URL}/api/state/refresh`, {
    method: 'POST',
  });
  const data = await res.json();
  return NextResponse.json(data, { status: res.status });
}
