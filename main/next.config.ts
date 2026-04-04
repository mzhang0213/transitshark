import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: "https://transitshark.mzhang.dev/api/:path*",
      },
    ];
  },
};

export default nextConfig;
