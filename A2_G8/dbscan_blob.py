import matplotlib.pyplot as plt
import numpy as np
from sklearn.datasets import make_blobs
from sklearn.cluster import KMeans, DBSCAN

# Generate isotropic Gaussian blobs for clustering
X_blobs, _ = make_blobs(n_samples=300, centers=3, cluster_std=0.60, random_state=42)

# Compute KMeans with k-means++
kmeans_blobs = KMeans(n_clusters=3, init='k-means++', random_state=42)
kmeans_blobs_labels = kmeans_blobs.fit_predict(X_blobs)

# Compute DBSCAN
dbscan_blobs = DBSCAN(eps=0.3, min_samples=5)
dbscan_blobs_labels = dbscan_blobs.fit_predict(X_blobs)

# Create colors for the plots
unique_labels_kmeans_blobs = set(kmeans_blobs_labels)
unique_labels_dbscan_blobs = set(dbscan_blobs_labels)

colors_kmeans_blobs = [plt.cm.Spectral(each) for each in np.linspace(0, 1, len(unique_labels_kmeans_blobs))]
colors_dbscan_blobs = [plt.cm.Spectral(each) for each in np.linspace(0, 1, len(unique_labels_dbscan_blobs))]

# Plot KMeans result
plt.figure(figsize=(12, 6))

plt.subplot(1, 2, 1)
for k, col in zip(unique_labels_kmeans_blobs, colors_kmeans_blobs):
    class_member_mask = (kmeans_blobs_labels == k)
    xy = X_blobs[class_member_mask]
    plt.plot(xy[:, 0], xy[:, 1], 'o', markerfacecolor=tuple(col),
             markeredgecolor='k', markersize=6)
plt.title('KMeans++ clustering on make_blobs dataset')
plt.xlabel('Feature 1')
plt.ylabel('Feature 2')

# Plot DBSCAN result
plt.subplot(1, 2, 2)
for k, col in zip(unique_labels_dbscan_blobs, colors_dbscan_blobs):
    if k == -1:
        col = [0, 0, 0, 1]  # Black used for noise.

    class_member_mask = (dbscan_blobs_labels == k)
    xy = X_blobs[class_member_mask]
    plt.plot(xy[:, 0], xy[:, 1], 'o', markerfacecolor=tuple(col),
             markeredgecolor='k', markersize=6)
plt.title('DBSCAN clustering on make_blobs dataset')
plt.xlabel('Feature 1')
plt.ylabel('Feature 2')

plt.tight_layout()
plt.show()