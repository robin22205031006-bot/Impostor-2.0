import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';

interface ErrorBoundaryState {
  hasError: boolean;
  message: string;
}

class AppErrorBoundary extends React.Component<
  { children: React.ReactNode },
  ErrorBoundaryState
> {
  constructor(props: { children: React.ReactNode }) {
    super(props);
    this.state = { hasError: false, message: '' };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return {
      hasError: true,
      message: error?.message || 'Unexpected rendering error.',
    };
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="flex min-h-screen flex-col items-center justify-center bg-[#1D1626] p-6 text-center text-[#F7F2EC]">
          <div className="w-full max-w-md rounded-3xl border border-[#FF3E5B]/60 bg-[#241C2F] p-6 shadow-2xl">
            <div className="font-mono-label text-xs font-extrabold uppercase tracking-[0.2em] text-[#FF7A8F]">
              SESSION RECOVERED
            </div>
            <h1 className="mt-2 font-display text-2xl font-black">
              Something interrupted the screen
            </h1>
            <p className="mt-2 text-xs text-[#B7ABC8]">{this.state.message}</p>
            <button
              type="button"
              onClick={() => window.location.reload()}
              className="mt-5 inline-flex h-11 items-center justify-center rounded-2xl bg-[#F4CD6C] px-6 font-display text-sm font-extrabold text-[#1D1626]"
            >
              Reload Game
            </button>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <AppErrorBoundary>
      <App />
    </AppErrorBoundary>
  </React.StrictMode>
);
