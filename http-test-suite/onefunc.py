#!/usr/bin/env python

import re
import socket
import httplib
import unittest

class HttpServer(unittest.TestCase):
  host = "localhost"
  port = 8080

  def setUp(self):
    self.conn = httplib.HTTPConnection(self.host, self.port, timeout=10)

  def tearDown(self):
    self.conn.close()

  def test_head_method(self):
    """head method support"""

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((self.host, self.port))
    s.send("HEAD /httptest/dir2/page.html HTTP/1.0\r\n\r\n")
    data = ""
    while 1:
      buf = s.recv(1024)
      if not buf: break
      data += buf
    s.close()
    
    self.assertTrue(data.find("\r\n\r\n") > 0, "no empty line with CRLF found")
    (head, body) = re.split("\r\n\r\n", data, 1);
    headers = head.split("\r\n");
    self.assertTrue(len(headers) > 0, "no headers found")
    statusline = headers.pop(0)
    print(statusline)
    (proto, code, status) = statusline.split(" ");
    h = {}
    for k,v in enumerate(headers):
      (name, value) = re.split('\s*:\s*', v, 1)
      h[name] = value
    if (int(code) == 200):
      self.assertEqual(int(h['Content-Length']), 38)
      self.assertEqual(len(body), 0)
    else:
      self.assertIn(int(code), (400,405))


loader = unittest.TestLoader()
suite = unittest.TestSuite()
a = loader.loadTestsFromTestCase(HttpServer)
suite.addTest(a)

class NewResult(unittest.TextTestResult):
  def getDescription(self, test):
    doc_first_line = test.shortDescription()
    return doc_first_line or ""

class NewRunner(unittest.TextTestRunner):
  resultclass = NewResult

runner = NewRunner(verbosity=2)
runner.run(suite)

