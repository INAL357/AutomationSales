// A popup window. Use it like:
//   {showForm && (
//     <Modal title="New customer" onClose={() => setShowForm(false)}>
//       ...content...
//     </Modal>
//   )}
import { useEffect } from 'react';

export default function Modal({ title, onClose, size = '', children }) {
  // Close the popup when Escape is pressed
  useEffect(() => {
    function handleKey(event) {
      if (event.key === 'Escape') onClose();
    }
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey); // clean up when the modal closes
  }, [onClose]);

  return (
    // Clicking the dark area outside the white box also closes it
    <div className="modal-overlay" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}>
      <div className={'modal ' + size} role="dialog" aria-modal="true" aria-label={title}>
        <div className="modal-header">
          <h2>{title}</h2>
          <button type="button" className="icon-button" onClick={onClose} aria-label="Close">✕</button>
        </div>
        <div className="modal-body">{children}</div>
      </div>
    </div>
  );
}
