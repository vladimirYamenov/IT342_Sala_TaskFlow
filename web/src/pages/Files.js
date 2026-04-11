import { useCallback, useEffect, useRef, useState } from 'react';
import { fileApi } from '../api';

export default function Files({ addToast }) {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');
  const fileInputRef = useRef(null);

  const fetchFiles = useCallback(async () => {
    try {
      const data = await fileApi.list();
      setFiles(Array.isArray(data) ? data : []);
    } catch {
      setFiles([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchFiles(); }, [fetchFiles]);

  const handleUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Client-side validation
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'application/pdf'];
    if (!allowedTypes.includes(file.type)) {
      addToast('Only images (JPEG, PNG, GIF, WebP) and PDFs are allowed.', 'error');
      if (fileInputRef.current) fileInputRef.current.value = '';
      return;
    }
    const maxSize = 10 * 1024 * 1024; // 10MB
    if (file.size > maxSize) {
      addToast('File size must be less than 10MB.', 'error');
      if (fileInputRef.current) fileInputRef.current.value = '';
      return;
    }

    setUploading(true);
    setError('');
    try {
      await fileApi.upload(file);
      addToast(`"${file.name}" uploaded successfully!`, 'success');
      fetchFiles();
    } catch (err) {
      const msg = err.message || 'Upload failed. Please try again.';
      setError(msg);
      addToast(msg, 'error');
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const handleDelete = async (file) => {
    try {
      await fileApi.delete(file.id);
      addToast(`"${file.fileName}" deleted.`, 'info');
      fetchFiles();
    } catch (err) {
      addToast(err.message || 'Failed to delete file.', 'error');
    }
  };

  const formatSize = (bytes) => {
    if (!bytes) return '—';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1048576) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / 1048576).toFixed(1)} MB`;
  };

  const fileIcon = (type) => {
    if (type?.includes('image')) return '🖼️';
    if (type?.includes('pdf')) return '📄';
    return '📎';
  };

  if (loading) return <div className="page-loader">Loading files...</div>;

  return (
    <div className="page-content fade-in">
      <div className="page-header">
        <div>
          <h1>Files</h1>
          <p className="page-subtitle">{files.length} file{files.length !== 1 ? 's' : ''}</p>
        </div>
        <div>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/jpeg,image/png,image/gif,image/webp,application/pdf"
            onChange={handleUpload}
            style={{ display: 'none' }}
            id="file-upload"
          />
          <button
            className="btn btn-primary"
            onClick={() => fileInputRef.current?.click()}
            disabled={uploading}
          >
            {uploading ? 'Uploading...' : '+ Upload File'}
          </button>
        </div>
      </div>

      {error && <p className="alert error">{error}</p>}

      <p className="text-muted" style={{ marginBottom: 16 }}>
        Accepted formats: JPEG, PNG, GIF, WebP, PDF — Max size: 10MB
      </p>

      {files.length === 0 ? (
        <div className="empty-state-box">
          <p className="empty-state-title">No files uploaded</p>
          <p className="text-muted">Upload images or PDFs to get started.</p>
          <button
            className="btn btn-primary"
            onClick={() => fileInputRef.current?.click()}
          >
            + Upload File
          </button>
        </div>
      ) : (
        <div className="file-grid">
          {files.map((file) => (
            <div className="file-card" key={file.id}>
              <div className="file-card-icon">{fileIcon(file.fileType)}</div>
              <div className="file-card-info">
                <strong className="file-card-name" title={file.fileName}>{file.fileName}</strong>
                <span className="text-muted">{formatSize(file.fileSize)}</span>
              </div>
              <div className="file-card-actions">
                <a
                  href={fileApi.downloadUrl(file.id)}
                  className="icon-btn"
                  title="Download"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  ⬇️
                </a>
                <button className="icon-btn icon-btn-danger" onClick={() => handleDelete(file)} title="Delete">🗑️</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
