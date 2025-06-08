import React from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom';
import Navigation from './components/Navigation';
import Home from './pages/Home';
import Chat from './pages/Chat';
import About from './pages/About';
import Login from './pages/Login';
import Register from './pages/Register';
import './App.css';

function AppContent() {
  const location = useLocation();
  const isChatPage = location.pathname === '/chat';
  const isAuthPage = location.pathname === '/login' || location.pathname === '/register';

  return (
    <div className="App">
      {!isChatPage && !isAuthPage && <Navigation />}
      <main className={`main-content ${isChatPage ? 'chat-mode' : ''} ${isAuthPage ? 'auth-mode' : ''}`}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/chat" element={<Chat />} />
          <Route path="/about" element={<About />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
        </Routes>
      </main>
    </div>
  );
}

function App() {
  return (
    <Router>
      <AppContent />
    </Router>
  );
}

export default App;
