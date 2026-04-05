"""
Plot zone score data exported by ZoneService.getZoneScores().

Usage:
    python plot_zone_scores.py                     # plots the most recent CSV
    python plot_zone_scores.py zone_scores_XXX.csv # plots a specific CSV
"""

import sys
import glob
import os
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
import numpy as np

def find_latest_csv():
    csvs = sorted(glob.glob(os.path.join(os.path.dirname(__file__), "zone_scores_*.csv")))
    if not csvs:
        print("No zone_scores_*.csv files found in logs/")
        sys.exit(1)
    return csvs[-1]

def main():
    csv_path = sys.argv[1] if len(sys.argv) > 1 else find_latest_csv()
    print(f"Reading: {csv_path}")
    df = pd.read_csv(csv_path)
    df['service'] = (df['service']) / (np.max(df['service']))

    fig, axes = plt.subplots(2, 2, figsize=(16, 12))
    fig.suptitle(f"Zone Scores — {os.path.basename(csv_path)}", fontsize=14, fontweight="bold")

    # 1. Spatial heatmap of zone scores
    ax = axes[0, 0]
    sc = ax.scatter(df["center_lng"], df["center_lat"], c=df["score"],
                    cmap="RdYlGn_r", s=12, edgecolors="none", vmin=-1, vmax=1)
    fig.colorbar(sc, ax=ax, label="Zone Score")
    ax.set_xlabel("Longitude")
    ax.set_ylabel("Latitude")
    ax.set_title("Zone Scores (spatial)")
    ax.set_aspect("equal")

    # 2. Demand vs Service scatter
    ax = axes[0, 1]
    colors = ax.scatter(df["demand"], df["service"], c=df["score"],
                        cmap="RdYlGn_r", s=10, edgecolors="none", vmin=-1, vmax=1)
    fig.colorbar(colors, ax=ax, label="Zone Score")
    ax.plot([0, 1], [0, 1], "k--", alpha=0.3, label="demand = service")
    ax.set_xlabel("Demand")
    ax.set_ylabel("Service")
    ax.set_title("Demand vs Service")
    ax.legend(fontsize=8)

    # 3. Score distribution histogram
    ax = axes[1, 0]
    ax.hist(df["score"], bins=30, color="#4a90d9", edgecolor="white", alpha=0.85)
    ax.axvline(df["score"].median(), color="red", linestyle="--", label=f'Median: {df["score"].median():.1f}')
    ax.axvline(df["score"].mean(), color="orange", linestyle="--", label=f'Mean: {df["score"].mean():.1f}')
    ax.set_xlabel("Zone Score")
    ax.set_ylabel("Count")
    ax.set_title("Score Distribution")
    ax.legend(fontsize=8)

    # 4. Demand & Service spatial maps side by side
    ax = axes[1, 1]
    gap = df["demand"] - df["service"]
    sc = ax.scatter(df["center_lng"], df["center_lat"], c=gap,
                    cmap="RdBu_r", s=12, edgecolors="none",
                    vmin=-0.5, vmax=0.5)
    fig.colorbar(sc, ax=ax, label="Demand - Service (gap)")
    ax.set_xlabel("Longitude")
    ax.set_ylabel("Latitude")
    ax.set_title("Demand-Service Gap (spatial)")
    ax.set_aspect("equal")

    plt.tight_layout()
    out_path = csv_path.replace(".csv", ".png")
    plt.savefig(out_path, dpi=150)
    print(f"Saved: {out_path}")
    plt.show()

if __name__ == "__main__":
    main()
