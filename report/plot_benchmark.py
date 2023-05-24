import matplotlib.pyplot as plt

render_times_concurrent = [22.89, 82.52, 127.76, 189.00, 299.76]
render_times_sequential = [32.82, 128.39, 205.64, 283.62, 498.39]
resolutions = ["640x360", "1280x720", "1600x900", "1920x1080", "2560x1440"]
fig, ax = plt.subplots()
ax.plot(
    resolutions, render_times_concurrent, color="red", marker="o", label="Concurrent"
)
ax.plot(
    resolutions, render_times_sequential, color="blue", marker="o", label="Sequential"
)
ax.set_ylabel("Render time [s]")
ax.set_xlabel(
    """Standard 16:9 resolution

CPU render speed comparison between sequential and concurrent 
approaches to raytracing implemented in Scala using Akka. 
Tests were conducted on a PC with Intel Core i5-8600k processor."""
)

ax.legend(loc="best")
fig.savefig("benchmark.png", dpi=500, bbox_inches="tight")
