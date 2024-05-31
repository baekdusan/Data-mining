import matplotlib.pyplot as plt
import numpy as np
from sklearn.datasets import make_moons
from sklearn.cluster import KMeans, DBSCAN

# Generate sample data
X, _ = make_moons(n_samples=300, noise=0.1, random_state=42)

# Compute KMeans with k-means++
kmeans = KMeans(n_clusters=2, init='k-means++', random_state=42)
kmeans_labels = kmeans.fit_predict(X)

# Compute DBSCAN
dbscan = DBSCAN(eps=0.2, min_samples=5)
dbscan_labels = dbscan.fit_predict(X)

# Create colors for the plots
unique_labels_kmeans = set(kmeans_labels)
unique_labels_dbscan = set(dbscan_labels)

colors_kmeans = [plt.cm.Spectral(each) for each in np.linspace(0, 1, len(unique_labels_kmeans))]
colors_dbscan = [plt.cm.Spectral(each) for each in np.linspace(0, 1, len(unique_labels_dbscan))]

# Plot KMeans result
plt.figure(figsize=(12, 6))

plt.subplot(1, 2, 1)
for k, col in zip(unique_labels_kmeans, colors_kmeans):
    class_member_mask = (kmeans_labels == k)
    xy = X[class_member_mask]
    plt.plot(xy[:, 0], xy[:, 1], 'o', markerfacecolor=tuple(col),
             markeredgecolor='k', markersize=6)
plt.title('KMeans++ clustering on make_moons dataset')
plt.xlabel('Feature 1')
plt.ylabel('Feature 2')

# Plot DBSCAN result
plt.subplot(1, 2, 2)
for k, col in zip(unique_labels_dbscan, colors_dbscan):
    if k == -1:
        col = [0, 0, 0, 1]  # Black used for noise.

    class_member_mask = (dbscan_labels == k)
    xy = X[class_member_mask]
    plt.plot(xy[:, 0], xy[:, 1], 'o', markerfacecolor=tuple(col),
             markeredgecolor='k', markersize=6)
plt.title('DBSCAN clustering on make_moons dataset')
plt.xlabel('Feature 1')
plt.ylabel('Feature 2')

plt.tight_layout()
plt.show()